package apip.site;

import java.io.IOException;

import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import apip.util.SimpleCommandLineParser;

public class MyClassicSite {
	private MyClassicSiteHelper sitesHelper;

	public static final String APP_NAME = "content-notification";

	/**
	 * The message for displaying the usage parameters.
	 */
	private static final String[] USAGE_MESSAGE = { "Usage: --domain --siteName --toEmail --daysBefore" };

	/**
	 * Welcome message.
	 */
	private static final String[] WELCOME_MESSAGE = { "", "--Google Classic Sites Feeds Data API Java Demo--", "" };

	/**
	 * Constructor
	 * 
	 * @throws AuthenticationException
	 */
	public MyClassicSite(String appName, String domain, String siteName, String toEmail, int daysBefore)
			throws AuthenticationException {
		
		//if proxy needed to access outside telus, please uncomment these two lines
		//System.setProperty("https.proxyHost", "pac.tsl.telus.com");
		//System.setProperty("https.proxyPort", "8080");
		
		sitesHelper = new MyClassicSiteHelper(appName, domain, siteName, toEmail, daysBefore);

		try {
			sitesHelper.login();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints out a given message.
	 *
	 * @param msg the message to be printed.
	 */
	private static void printMessage(String[] msg) {
		for (String s : msg)
			System.out.println(s);
	}

	/**
	 * Starts up the demo and prompts for commands.
	 *
	 * @throws ServiceException
	 * @throws IOException
	 */
	public void run() throws IOException, ServiceException {
		printMessage(WELCOME_MESSAGE);

		sitesHelper.getActivityFeed();

	}

	/**
	 * Runs the demo.
	 *
	 * @param args the command-line arguments
	 *
	 */
	public static void main(String[] args) {
		SimpleCommandLineParser parser = new SimpleCommandLineParser(args);
		String domain = parser.getValue("domain", "d");
		String siteName = parser.getValue("siteName", "site", "s");
		String toEmail = parser.getValue("toEmail", "to");
		int daysBefore = Integer.parseInt(parser.getValue("daysBefore", "days"));

		// If domain is not set, use "site" for a non-Google Apps hosted Site.
		if (domain == null) {
			domain = "site";
		}
		
		if (siteName == null) {
			printMessage(USAGE_MESSAGE);
			System.exit(1);
		}
		
		if (toEmail == null) {
			printMessage(USAGE_MESSAGE);
			System.exit(1);
		}

		MyClassicSite demo = null;
		try {
			demo = new MyClassicSite(APP_NAME, domain, siteName, toEmail, daysBefore);
			demo.run();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AuthenticationException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		}
	}
}
