/**
 * Copyright: 2009 FreeCode AS
 * Project: trumpeter
 * Created: Jul 6, 2009
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; version 3.
 */
package no.freecode.trumpeter;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

/**
 * General configuration for the whole application.
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public class Configuration {

	private static final Logger logger = Logger.getLogger(Configuration.class);
	
	public static final String RULE_CACHE_REGION = "rules";
	
	private String rtBaseUrl;
	private String rtViewTicketUrl;  // may be overridden


	public String getRtBaseUrl() {
		return rtBaseUrl;
	}

	@Required
	public void setRtBaseUrl(String rtBaseUrl) {
		this.rtBaseUrl = rtBaseUrl;
	}
	

	public String getRtViewTicketUrl() {
		if (this.rtViewTicketUrl == null) {
			return this.rtBaseUrl + "/Ticket/Display.html?id=";
		} else {
			return rtViewTicketUrl;
		}
	}

	/**
	 * You may want to override the default view url (the url that is presented
	 * to the message receivers), but it's usually not necessary.
	 * 
	 * @param rtViewTicketUrl
	 */
	public void setRtViewTicketUrl(String rtViewTicketUrl) {
		this.rtViewTicketUrl = rtViewTicketUrl;
	}
}
