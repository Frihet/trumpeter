/**
 * Copyright: 2009 FreeCode AS
 * Project: trumpeter
 * Created: Jul 5, 2009
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; version 3.
 */
package no.freecode.trumpeter.rt;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

/**
 * Class representing a single RT ticket.
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public class Ticket {

    private static final Logger logger = Logger.getLogger(Ticket.class);
    
    private Map<String, String> ticketData;

    /**
     * Create  a new ticket based on textual input from the RT REST interface.
     */
    public Ticket(Map<String, String> ticketData) {
        this.ticketData = ticketData;
    }

    public String getStringProperty(String key) {
        return this.ticketData.get(key);
    }

    /**
     * For testing. Do not rely on this.
     */
    protected Map<String, String> getTicketData() {
        return this.ticketData;
    }

    /**
	 * 
	 */
	public String getId() {
		String[] splitId = StringUtils.split(this.ticketData.get("id"), '/');

		if (splitId.length == 2) {
			return splitId[1];
		} else {
			return null;
		}
	}
    
    /**
     * @return The created date, or <code>null</code> if it wasn't able to parse
     *         the date, or if it's not there.
     */
    public Date getCreatedDate() {
        String createdDateString = this.ticketData.get("Created");

        try {
            return DateUtils.parseDate(createdDateString, new String[] {RtParser.RT_DATE_FORMAT});
            
        } catch (ParseException e) {
            logger.error("Unable to parse date '" + createdDateString + "'.", e);
            return null;
        }
    }
}
