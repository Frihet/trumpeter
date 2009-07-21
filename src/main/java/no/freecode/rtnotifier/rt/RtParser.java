/**
 * Copyright: 2009 FreeCode AS
 * Project: rtnotifier
 * Created: Jul 5, 2009
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; version 3.
 */
package no.freecode.rtnotifier.rt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * Helper class to parse data received from the RT REST interface.
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public abstract class RtParser {

    private static final Logger logger = Logger.getLogger(RtParser.class);

    public static final String RT_DATE_FORMAT = "EEE MMM dd HH:mm:ss yyyy";
    
    /**
     * Read textual ticket descriptions from an input stream. The inserted data
     * will never be <code>null</code> (empty strings are used instead).
     * 
     * @param stream
     * @return The parsed tickets.
     */
    public static List<Ticket> parseTicketStream(InputStream stream) {
        ArrayList<Ticket> tickets = new ArrayList<Ticket>();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("id: ")) {
                    
                    // Ok, we have the beginning of a ticket. Collect the data.
                    Map<String, String> ticketData = new HashMap<String, String>();

                    String[] lineData = line.split(": ", 2);
                    if (lineData.length == 2) {
                        do {
                            ticketData.put(lineData[0], StringUtils.trimToEmpty(lineData[1]));

                        } while ((line = reader.readLine()) != null &&
                                 (lineData = (line + " ").split(": ", 2)).length == 2);

                        // Create a new Ticket based on the data, and add it to the returned list.
                        tickets.add(new Ticket(ticketData));
                    }
                }
            }

        } catch (IOException e) {
            logger.error("Trouble reading ticket data: " + e.getMessage());
        }
        
        return tickets;
    }
}
