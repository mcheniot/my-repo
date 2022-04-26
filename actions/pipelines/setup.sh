# install python
merge_files() {
    yq eval-all 'select(fileIndex == 0) * select(fileIndex == 1)' $1 $2 > $2.tmp
    if [ -z $3 ]; then 
        mv $2.tmp $2
    else 
        mv $2.tmp $3
    fi
}

function warn(){
    ORANGE='\033[0;33m'
    NC='\033[0m'
    HEADER_COLOR=$ORANGE MSG_COLOR=$ORANGE
    printf "${HEADER_COLOR}[%-5.5s]${NC} ${MSG_COLOR}%b${NC}" "WARN" "$1\n"
}

function error(){
    RED='\033[0;31m'
    NC='\033[0m'
    printf "${RED}[%-5.5s]${NC} ${RED}%b${NC}" "ERROR" "$1\n"
}

function info(){
    GREEN='\033[0;32m'
    NC='\033[0m'
    printf "${GREEN}[%-5.5s]${NC} ${GREEN}%b${NC}" "INFO" "$1\n"
}

function field_checking(){
    if `jq -r --arg key $1 '.[$key]' < /tmp/templating/cookiecutter.json > /dev/null`; then
        jq --arg KEY $1 --arg VAL $2 '.[$KEY]=$VAL' < /tmp/templating/cookiecutter.json > /tmp/templating/cookiecutter-tmp.json && mv /tmp/templating/cookiecutter-tmp.json /tmp/templating/cookiecutter.json
    fi
}


info "Installing dependencies..."
sudo apt update
sudo apt install software-properties-common
sudo add-apt-repository ppa:deadsnakes/ppa
sudo apt update
sudo apt install python3.8
pip3 install -q --user --upgrade cookiecutter


# adding json validation
pip3 install -q jsonschema

# install yq
wget -q https://github.com/mikefarah/yq/releases/download/v4.2.0/yq_linux_amd64 -O /usr/bin/yq
chmod u+x /usr/bin/yq

# install jq
sudo apt-get install -q jq


if [[ -z $SOURCE_REPO ]]; then
    $SOURCE_REPO="gcp-pipeline-templates"
fi

# TODO: add latest source code
if [[ -z $PIPELINE_TAG ]]; then 
    PIPELINE_TAG=$(
    git -c 'versionsort.suffix=-' \
        ls-remote --exit-code --refs --sort='version:refname' --tags https://$GITHUB_PAT@github.com/telus/$SOURCE_REPO.git "pipelines-[0-9]*" \
        | tail --lines=1 \
        | cut --delimiter='/' --fields=3
    )
fi

# install gh cli
curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
sudo apt update
sudo apt install gh
echo "$GITHUB_PAT" | gh auth login --with-token

# Clone pipeline repo
git clone --depth 1 --branch $PIPELINE_TAG https://$GITHUB_PAT@github.com/telus/$SOURCE_REPO.git

if ! test "$?" -eq 0
then
    error >&2 
    error "command failed with exit status $?"
    exit 1
fi

#Checking the language
if [ $(find main -name package.json | wc -l) -ge 1 ]; then
    LANGUAGE="JAVASCRIPT";
else
    LANGUAGE="JAVA";
fi

# Copying the files
mkdir -p /tmp/templating/{{cookiecutter.application}}
cp -r $SOURCE_REPO/doe-artifacts/helm /tmp/templating/{{cookiecutter.application}}
cp -r $SOURCE_REPO/doe-artifacts/workflows /tmp/templating/{{cookiecutter.application}}
cp $SOURCE_REPO/doe-artifacts/cloudbuild.yaml /tmp/templating/{{cookiecutter.application}}
cp $SOURCE_REPO/doe-artifacts/Dockerfile /tmp/templating/{{cookiecutter.application}}

# Check if cookiecutter file exists or exit
if [[ -z `find main/ -name cookiecutter.json` ]]; then
    error "cookiecutter.json file not found"
    exit 1
fi

cat $(find main/ -name cookiecutter.json) >> /tmp/templating/cookiecutter.json

# the lonely double quote below is not an error, the string passed to -F doesn't recognize 
# line jump, it's for formatting only you can append it at the end of the command
info "running schema validation..."
jsonschema -i /tmp/templating/cookiecutter.json $ACTION_DIR/models/v3.schema -F "ERROR:{error.message}
"
if [ $? -ne 0 ]; then 
    error "READ ABOVE FOR THE ERRORS..."
    exit 1
fi

# add language
jq '.language='\"$LANGUAGE\"'' < /tmp/templating/cookiecutter.json > /tmp/templating/cookiecutter-tmp.json && mv /tmp/templating/cookiecutter-tmp.json /tmp/templating/cookiecutter.json

# Default values
# Let's check for default values
info "Checking default values"
# cluster fields np
field_checking "cluster_name_np" "private-na-ne1-001"
field_checking "cluster_location_np" "northamerica-northeast1"
field_checking "cluster_project_id_np" "cdo-gke-private-np-1a8686"
# cluster fields pr
field_checking "cluster_name_pr" "private-na-ne1-001"
field_checking "cluster_location_pr" "northamerica-northeast1"
field_checking "cluster_project_id_pr" "cdo-gke-private-pr-7712d7"

field_checking "devops_project_id" "cio-gke-devops-e4993356"
field_checking "wif_provider" "projects/1022893644241/locations/global/workloadIdentityPools/cdo-github-wif/providers/cdo-github-provider"
# we must change this once we have a working chart
field_checking "helm_repo" "northamerica-northeast1-docker.pkg.dev/cio-gke-devops-e4993356/charts"
field_checking "chart_name" "cio-gke-deployment"
field_checking "chart_version" "1.0.0"

# Check chart version
if [[ `jq -r '.chart' < /tmp/templating/cookiecutter.json` != "" ]] && [[ `jq -r '.chart' < /tmp/templating/cookiecutter.json` != "cdo-gke-deployment" ]] \
        && [[ `jq -r '.chart_version' < /tmp/templating/cookiecutter.json` < "1.0.0" ]]; then
	warn 'This version of the action requires chart version 1.0.0 or higher, if you need an specific chart version, specify it in the chart value'
    info 'Overriding chart version to 1.0.0 and chart to cdo-gke-deployment'
    jq '.chart_name = "cdo-gke-deployment"' /tmp/templating/cookiecutter.json > /tmp/templating/cookiecutter-tmp.json && mv /tmp/templating/cookiecutter-tmp.json /tmp/templating/cookiecutter.json
    jq '.chart_version = "1.0.0"' /tmp/templating/cookiecutter.json > /tmp/templating/cookiecutter-tmp.json && mv /tmp/templating/cookiecutter-tmp.json /tmp/templating/cookiecutter.json
fi



# Get folder name 
FOLDER=$(cat /tmp/templating/cookiecutter.json | jq .application)
FOLDER=$(sed -e 's/^"//'  -e 's/"$//' <<< $FOLDER)
#export $FOLDER 

cookiecutter /tmp/templating/. --no-input --output-dir /tmp/templating/cookiecutter-temp || {
    exit 1
}

# Check if directory exists
if [[ ! -d main/$OUTPUT_VALUES_PATH ]]; then 
    mkdir -p main/$OUTPUT_VALUES_PATH
fi

info "Moving generated files to specified outputs"
# check if file exists to merge them
# checking st values file
if [ -f main/$OUTPUT_VALUES_PATH/$FOLDER-st.yaml ]; then 
    merge_files /tmp/templating/cookiecutter-temp/$FOLDER/helm/$FOLDER-st.yaml main/$OUTPUT_VALUES_PATH/$FOLDER-st.yaml 
else 
    mv /tmp/templating/cookiecutter-temp/$FOLDER/helm/$FOLDER-st.yaml main/$OUTPUT_VALUES_PATH/
fi

# checking pr values file
if [ -f main/$OUTPUT_VALUES_PATH/$FOLDER-pr.yaml ]; then
    merge_files /tmp/templating/cookiecutter-temp/$FOLDER/helm/$FOLDER-pr.yaml main/$OUTPUT_VALUES_PATH/$FOLDER-pr.yaml
else     
    mv /tmp/templating/cookiecutter-temp/$FOLDER/helm/$FOLDER-pr.yaml main/$OUTPUT_VALUES_PATH/        
fi

# Other values files
mv /tmp/templating/cookiecutter-temp/$FOLDER/helm/* main/$OUTPUT_VALUES_PATH
# mv /tmp/templating/cookiecutter-temp/$FOLDER/helm/$FOLDER-cache-st.yaml main/$OUTPUT_VALUES_PATH

# echo In this point we are overidding the cloudbuild.yaml
mv /tmp/templating/cookiecutter-temp/$FOLDER/cloudbuild.yaml main/
mv /tmp/templating/cookiecutter-temp/$FOLDER/Dockerfile main/

# Get current date and time
now="$(date +'%s')"

# Copy workflows
cd main
mkdir -p .github/workflows
cp /tmp/templating/cookiecutter-temp/$FOLDER/workflows/* .github/workflows/

# Git push
git config --global user.name "github-actions[bot]"
git config --global user.email "github-actions[bot]@users.noreply.github.com"
git checkout -b "$PIPELINE_TAG-$now"
git add .
git commit -m "chore: $PIPELINE_TAG generated by github actions"
#git push --set-upstream origin "$PIPELINE_TAG-$now"
git push https://username:$GITHUB_PAT@github.com/$GITHUB_REPOSITORY.git

#create a PR
gh pr create --title "feat: CICD Example Setup" -F ../$SOURCE_REPO/PR.md --head "$PIPELINE_TAG-$now"