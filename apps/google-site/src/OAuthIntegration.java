import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.gdata.client.sites.SitesService;
import com.google.gdata.data.sites.ActivityFeed;
import com.google.gdata.data.sites.BaseActivityEntry;
import com.google.gdata.util.ServiceException;

public class OAuthIntegration {
	public static void main(String[] args)
			throws MalformedURLException, GeneralSecurityException, IOException, ServiceException {
		URL SITE_FEED_URL;
		SITE_FEED_URL = new URL("https://sites.google.com/feeds/activity/site/aaipsite");

		File p12 = new File("c:/work/key.p12");

		HttpTransport httpTransport = new NetHttpTransport();
		JacksonFactory jsonFactory = new JacksonFactory();
		String[] SCOPESArray = {"https://sites.google.com/feeds"};
		final List SCOPES = Arrays.asList(SCOPESArray);
		
		GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(jsonFactory)
				.setServiceAccountId("107023185254985248806").setServiceAccountScopes(SCOPES)
				.setServiceAccountPrivateKeyFromP12File(p12).build();
		
		SitesService service = new SitesService("aaipsite");

		service.setOAuth2Credentials(credential);
		
		ActivityFeed activityFeed = service.getFeed(SITE_FEED_URL, ActivityFeed.class);
		for (BaseActivityEntry<?> entry : activityFeed.getEntries()) {
			System.out.println(entry.getSummary().getPlainText());
			System.out.println(" revisions link: " + entry.getRevisionLink().getHref());
		}

//		if (spreadsheets.size() == 0) {
//			System.out.println("No spreadsheets found.");
//		}
//
//		SpreadsheetEntry spreadsheet = null;
//		for (int i = 0; i < spreadsheets.size(); i++) {
//			if (spreadsheets.get(i).getTitle().getPlainText().startsWith("ListOfSandboxes")) {
//				spreadsheet = spreadsheets.get(i);
//				System.out.println("Name of editing spreadsheet: " + spreadsheets.get(i).getTitle().getPlainText());
//				System.out.println("ID of SpreadSheet: " + i);
//			}
//
//		}
	}
}
/**
guava-11.0.2.jar
gdata-spreadsheet-3.0.jar
gdata-maps-2.0.jar
gdata-core-1.0.jar
jackson-core-asl-1.9.11.jar
jackson-core-2.1.3.jar
google-oauth-client-1.20.0.jar
google-http-client-jackson2-1.20.0.jar
google-http-client-jackson-1.20.0.jar
google-http-client-1.20.0.jar
google-api-client-1.20.0.jar
*/