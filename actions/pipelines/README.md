# pipeline internal documentation pending 

This is the yaml file you need to copy and paste into your default branch

```yaml
name: GithubActions

on: workflow_dispatch

jobs:
  pipeline-update:
    runs-on: ubuntu-18.04
    steps:
      - uses: actions/checkout@v2
        with:
          path: main
      - uses: actions/checkout@v2                            
        with:
          repository: telus/doe-tools
          token: ${{ secrets.READ_PAT }}
          path: actions
          ref: v0.1.0
      - name: Private Action
        uses: ./actions/actions/pipelines
        with: 
          github-token: ${{ secrets.READ_PAT }}
```