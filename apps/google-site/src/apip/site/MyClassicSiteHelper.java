package apip.site;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.gdata.client.sites.SitesService;
import com.google.gdata.data.XhtmlTextConstruct;
import com.google.gdata.data.sites.ActivityFeed;
import com.google.gdata.data.sites.BaseActivityEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import apip.util.MailSender;

/**
 * Wrapper class for lower level Sites activity API calls.
 *
 * 
 */
public class MyClassicSiteHelper {

	private String domain;
	private String siteName;
	private String toEmail;
	private int daysBefore;

	public SitesService service;
	

	/**
	 * Constructor
	 *
	 * @param applicationName The name of the application.
	 * @param domain          The Site's Google Apps domain or "site".
	 * @param siteName        The webspace name of the Site.
	 * @param toEmail		  The recipient for updated content
	 * @param daysBefore	  The days within that contents have been updated
	 */
	public MyClassicSiteHelper(String applicationName, String domain, String siteName, String toEmail, int daysBefore) {
		this.domain = domain;
		this.siteName = siteName;
		this.service = new SitesService(applicationName);
		this.toEmail = toEmail;
		this.daysBefore = daysBefore;
	
	}

	

	/**
	 * Authenticates the user 
	 *
	 * @throws AuthenticationException
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public void login() throws AuthenticationException, GeneralSecurityException, IOException {
		service.setOAuth2Credentials(getCredential());
	}

	private GoogleCredential getCredential() throws GeneralSecurityException, IOException {
		//Must generate key file and accountId of Service Account
		
		//path to key file
		File p12 = new File("c:/temp/pkey.p12");
		HttpTransport httpTransport = new NetHttpTransport();
		JacksonFactory jsonFactory = new JacksonFactory();
		String[] SCOPESArray = { "https://sites.google.com/feeds" };
		final List<String> SCOPES = Arrays.asList(SCOPESArray);

		GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
				.setJsonFactory(jsonFactory).setServiceAccountId("Your Service Account ID")
				.setServiceAccountScopes(SCOPES).setServiceAccountPrivateKeyFromP12File(p12).build();
		return credential;
	}

	public String getActivityFeedUrl() {
		return "https://sites.google.com/feeds/activity/" + domain + "/" + siteName + "/";
	}

	/**
	 * Fetches and displays the Site's activity feed.
	 */
	public void getActivityFeed() throws IOException, ServiceException {
		
		Calendar day = Calendar.getInstance();
		day.add(Calendar.DATE, -daysBefore);
		
		ActivityFeed activityFeed = service.getFeed(new URL(getActivityFeedUrl()), ActivityFeed.class);

		String body = null;
		
		for (BaseActivityEntry<?> entry : activityFeed.getEntries()) {
			
			//if content is updated within the past of "daysBefore" days
			if (entry.getUpdated().getValue() < day.getTimeInMillis())
				continue;
			
			String contentUrl = ((XhtmlTextConstruct) entry.getSummary()).getXhtml().getFullText();
			contentUrl = contentUrl.substring(contentUrl.indexOf("\n") + 1);
			contentUrl = contentUrl.substring(0, contentUrl.indexOf(" "));

			//content summary
			body += entry.getSummary().getPlainText() + " @ " + entry.getUpdated().toUiString() + "<br>";

			//updated content
			body += "content url: <a href=\"" + contentUrl + "\">" + contentUrl + "</a>" + "<br><br>";
			
		}

		//if there is any content updated, send out the email
		if (body != null) {
			body = "Hi,<br> the following are updated contents.<br><br>" + body;
			MailSender.sendEMail(body, toEmail);
		}

	}

}
