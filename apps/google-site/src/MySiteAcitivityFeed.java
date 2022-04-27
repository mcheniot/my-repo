import java.io.IOException;
import java.net.URL;

import com.google.gdata.client.sites.SitesService;
import com.google.gdata.data.sites.ActivityFeed;
import com.google.gdata.data.sites.BaseActivityEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class MySiteAcitivityFeed {
	
	SitesService client;

	public static void main(String[] args) {
		
		MySiteAcitivityFeed mySite = new MySiteAcitivityFeed();
		try {
			mySite.getActivityFeed();
		} catch (IOException | ServiceException e) {
			e.printStackTrace();
		}

	}

	public MySiteAcitivityFeed() {
		client = new SitesService("aaipsite");
		try {
			client.setUserCredentials("michael.chen@gmail.com", "G00dnite$");
		} catch (AuthenticationException e) {
			
			e.printStackTrace();
		}
	}

	public String buildActivityFeedUrl() {
		String domain = "telus.com"; // OR if the Site is hosted on G Suite, your domain (e.g. example.com)
		String siteName = "api-program";
		return "https://sites.google.com/feeds/activity/" + domain + "/" + siteName + "/";
	}

	public void getActivityFeed() throws IOException, ServiceException {
		ActivityFeed activityFeed = client.getFeed(new URL(buildActivityFeedUrl()), ActivityFeed.class);
		for (BaseActivityEntry<?> entry : activityFeed.getEntries()) {
			System.out.println(entry.getSummary().getPlainText());
			System.out.println(" revisions link: " + entry.getRevisionLink().getHref());
		}
	}

}
