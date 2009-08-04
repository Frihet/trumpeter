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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import no.freecode.trumpeter.rt.Cache;
import no.freecode.trumpeter.rt.Finalizer;
import no.freecode.trumpeter.rt.RtConnection;
import no.freecode.trumpeter.rt.Rule;
import no.freecode.trumpeter.rt.Ticket;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
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
    private Set<Rule> rules = Collections.emptySet();
    private Set<Finalizer> finalizers = Collections.emptySet();
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

    public Set<Finalizer> getFinalizers() {
        return finalizers;
    }

    public void setFinalizers(Set<Finalizer> finalizers) {
        this.finalizers = finalizers;
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
            		String message = rule.getMessage(ticket);
            		if (message != null) {
						// There is information to publish about this ticket. Send it out.
                        sendMessage(XmppUtils.createChatMessage(chat, message));
            		}
            	}
            }

            Set<Finalizer> finalizers = getFinalizers();
            for (Finalizer f : finalizers) {
                List<String> messages = f.getMessages();
                if (messages != null) {
                    for (String message : messages) {
                        // There is information to publish about this ticket. Send it out.
                        sendMessage(XmppUtils.createChatMessage(chat, message));
                    }
                }
            }

        } catch (HttpException e) {
        	logger.error(e);

        } catch (IOException e) {
        	logger.error(e);
        }
    }

    @PostConstruct
    public void updateRuleNamespaces() {
        if (getRules() != null) {
            for (Rule r : getRules()) {
                if (r instanceof Cache) {
                    /* Set the cache name space of all CachingRule objects. Use
                     * the chat room as a namespace (e.g.
                     * "test@conference.example.com"). I hope this is a
                     * sufficiently unique identifier...
                     */
                    ((Cache) r).setNamespace(getChatRoom());
                }
            }
        }

        if (getFinalizers() != null) {
            for (Finalizer f : getFinalizers()) {
                if (f instanceof Cache) {
                    /* Set the cache name space of all CachingRule objects. Use
                     * the chat room as a namespace (e.g.
                     * "test@conference.example.com"). I hope this is a
                     * sufficiently unique identifier...
                     */
                    ((Cache) f).setNamespace(getChatRoom());
                }
            }
        }
    }
}
