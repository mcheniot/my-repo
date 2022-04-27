package apip.site;

/**
 * Exception to be thrown when there is an issue with the SitesHelper class.
 */
public class MyClassicSiteException extends Exception {
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public MyClassicSiteException() {
    super();
  }

  public MyClassicSiteException(String msg) {
    super(msg);
  }
}
