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
import java.util.HashMap;
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
    private int hourOffset;

    /**
     * Create a new ticket based on textual input from the RT REST interface.
     */
    public Ticket(Map<String, String> ticketData, int hourOffset) {
        this.ticketData = ticketData;
        this.hourOffset = hourOffset;
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
	 * Get the ticket id (ticket number).
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
        return getDate("Created");
    }

    /**
     * Helper method to parse RT date strings.
     */
    private Date getDate(String key) {
        String createdDateString = this.ticketData.get(key);

        try {
            Date date = DateUtils.parseDate(createdDateString, new String[] {RtParser.RT_DATE_FORMAT});

            if (this.hourOffset == 0) {
                return date;
            } else {
                return DateUtils.addHours(date, this.hourOffset);
            }

        } catch (ParseException e) {
            logger.error("Unable to parse date '" + createdDateString + "'.", e);
            return null;
        }
    }

    /**
     * @return the number of hours difference between RT and this instance.
     */
    public int getHourOffset() {
        return hourOffset;
    }

    /**
     * @return The current ticket status.
     */
    public Status getStatus() {
        return Status.getStatus(ticketData.get("Status"));
    }

    /**
     * Set the number of hours difference between your RT instance and this
     * server. This is required if the RT is not in the same time zone as this
     * server, since RT does not return information about its timezone.
     */
    public void setHourOffset(int hourOffset) {
        this.hourOffset = hourOffset;
    }
    

    public enum Status {
        STALLED("stalled"),
        REJECTED("rejected"),
        RESOLVED("resolved"),

        NEW("new"),
        OPEN("open"),

        UNKNOWN("unknown"),
        ;

        private static Status DEFAULT_STATUS = UNKNOWN;
        private static HashMap<String, Status> idMap = new HashMap<String, Status>();
        
        static {
            for (Status s : Status.values()) {
                idMap.put(s.id, s);
            }
        }

        private String id;
        
        private Status(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return id;
        }

        public static Status getStatus(String id) {
            Status status = idMap.get(id);

            if (status == null) {
                return DEFAULT_STATUS;
            } else {
                return status;
            }
        }
    }
}
