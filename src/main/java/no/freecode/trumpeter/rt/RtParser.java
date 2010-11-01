/**
 * Copyright: 2010 FreeCode AS
 * Project: trumpeter
 * Created: Jul 5, 2009
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; version 3.
 */
package no.freecode.trumpeter.rt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.freecode.trumpeter.Configuration;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;


/**
 * Helper class to parse data received from the RT REST interface.
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public class RtParser {

    private static final Logger logger = Logger.getLogger(RtParser.class);

    public static final String RT_DATE_FORMAT = "EEE MMM dd HH:mm:ss yyyy";
    
    private Configuration configuration;
    
    /**
     * Read textual ticket descriptions from an input stream. The inserted data
     * will never be <code>null</code> (empty strings are used instead).
     * 
     * @param stream
     * @return The parsed tickets.
     * @throws HttpException
     */
    public List<Ticket> parseTicketStream(InputStream stream) throws HttpException {
        ArrayList<Ticket> tickets = new ArrayList<Ticket>();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        try {
            String line = reader.readLine();
            
            // Check the first line. This is where the RT status is returned.
            if (line != null) {
                // TODO: probably need more sofisticated error handling. Should return the specific error message.
                if (!line.contains("200")) {
                    throw new HttpException(line);  // TODO: make an RtException instead
                }
            }
            
            while (line != null) {
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
                        tickets.add(new Ticket(ticketData, configuration.getRtHourOffset()));
                    }
                }

                line = reader.readLine();
            }

        } catch (HttpException e) {
            throw e;

        } catch (IOException e) {
            logger.error("Trouble reading ticket data: " + e.getMessage());
        }
        
        return tickets;
    }

    @Autowired
    @Required
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
