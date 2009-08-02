/**
 * Copyright: 2009 FreeCode AS
 * Project: trumpeter
 * Created: Jul 4, 2009
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; version 3.
 */
package no.freecode.trumpeter.xmpp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

import no.freecode.trumpeter.rt.RtConnection;
import no.freecode.trumpeter.rt.Rule;
import no.freecode.trumpeter.rt.Ticket;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

/**
 * Handle notifications based on RT rules ({@link Rule}).
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public class TicketQueryAgent extends XmppChatAgent {

    private static final Logger logger = Logger.getLogger(TicketQueryAgent.class);
	
    private String query;
    private Set<Rule> rules;
    private RtConnection rtConnection;

    public String getQuery() {
        return query;
    }

    /**
     * Set the query. It will be URL-encoded with UTF-8 as part of this method.
     * 
     * @param query
     * @throws UnsupportedEncodingException
     *             if utf-8 doesn't exist (that would be pretty strange...).
     */
    @Required
    public void setQuery(String query) throws UnsupportedEncodingException {
        this.query = URLEncoder.encode(query, "utf-8");
    }

    public Set<Rule> getRules() {
        return rules;
    }

    @Required
    public void setRules(Set<Rule> rules) {
        this.rules = rules;
    }

    public RtConnection getRtConnection() {
        return rtConnection;
    }

    @Autowired
    @Required
    public void setRtConnection(RtConnection rtConnection) {
        this.rtConnection = rtConnection;
    }

    /* (non-Javadoc)
     * @see no.freecode.trumpeter.Agent#invoke()
     */
    @Override
    public void invoke() {
        try {
            List<Ticket> tickets = rtConnection.getTickets(getQuery());

            for (Ticket ticket : tickets) {
            	for (Rule rule : rules) {
            		String ticketMessage = rule.getMessage(ticket);
            		if (ticketMessage != null) {
						// There is information to publish about this ticket.
            		    Message message = XmppUtils.createChatMessage(chat, ticketMessage);
                        sendMessage(message);
            		}
            	}
            }

        } catch (HttpException e) {
        	logger.error(e);

        } catch (IOException e) {
        	logger.error(e);
        }
    }
}
