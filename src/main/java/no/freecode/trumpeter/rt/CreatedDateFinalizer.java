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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import no.freecode.trumpeter.rt.Ticket.Status;

import org.apache.log4j.Logger;
import org.jivesoftware.smackx.XHTMLText;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A {@link Finalizer} that examines the tickets that have been stored by
 * {@link CreatedDateRule}, and generates messages e.g. when they have been
 * taken or closed.
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public class CreatedDateFinalizer extends Cache implements Finalizer {

	private static final Logger logger = Logger.getLogger(CreatedDateFinalizer.class);

	private static final String TAKEN_MESSAGE_SENT = "taken_sent";

	
	@Autowired
	private RtConnection rtConnection;
	
    @Override
    public List<String> getMessages() {
        Set<String> watchlist = getWatchlist();
        List<String> removeFromWatchlist = new ArrayList<String>();
        List<String> messages = new ArrayList<String>();

        // XXX: right now it's fetching one ticket at a time. The best way of
        // doing this might be to create a long query with all the tickets in
        // watchlist (to a certain point - if the query gets too long, RT will
        // probably not be able to cope, so split it up into e.g. parts of max
        // 10 tickets).

        try {
            if (watchlist != null) {
                for (String ticketId : watchlist) {
                    Ticket ticket = rtConnection.getTicket(ticketId);
                    if (ticket != null) {
                        RuleCache ruleCache = getRuleCache(ticket);
                        Set<String> handled = ruleCache.getHandled();
                        
                        Status status = ticket.getStatus();

                        if (status != Status.NEW && status != Status.OPEN && status != Status.STALLED) {
                            XHTMLText xhtmlText = new XHTMLText(null, null);
                            xhtmlText.appendOpenStrongTag();
                            xhtmlText.append("Issue " + status + ": ");
                            xhtmlText.appendCloseStrongTag();
                            appendTicketDescription(xhtmlText, ticket);

                            if (status == Status.RESOLVED) {
                                xhtmlText.append(". Good work " + ticket.getStringProperty("Owner") + "!");
                            } else {
                                xhtmlText.append(". Owner was " + ticket.getStringProperty("Owner") + ".");
                            }

                            messages.add(xhtmlText.toString());

                            removeFromWatchlist.add(ticketId); // don't talk about this ticket any more...

                        } else if (!handled.contains(TAKEN_MESSAGE_SENT) && !"Nobody".equals(ticket.getStringProperty("Owner"))) {
                            XHTMLText xhtmlText = new XHTMLText(null, null);
                            appendTicketDescription(xhtmlText, ticket);
                            xhtmlText.append(" was taken by / given to " + ticket.getStringProperty("Owner") + ".");
                            messages.add(xhtmlText.toString());
                            handled.add(TAKEN_MESSAGE_SENT);
                        }

                        // Update cache so that we don't send the same message twice.
                        saveRuleCache(ruleCache, ticket);

                    } else {
                        // If there is no ticket with that id, don't do anything
                        // else. Remove it from the list so that it won't be checked
                        // again.
                        removeFromWatchlist.add(ticketId);
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("An error occurred when manipulating the watch list: " + e.getMessage());
        }

        // Clean up watchlist (couldn't do this inside the loop, as that would have thrown a ConcurrentException).
        for (String ticketId : removeFromWatchlist) {
            watchlist.remove(ticketId);
        }

        saveWatchlist(watchlist);
        return messages;
    }

    /**
     * TODO: remove from here - it's already defined in CreatedDateRule...
     * 
     * Add a ticket description / link to an {@link XHTMLText}.
     */
    private void appendTicketDescription(XHTMLText xhtmlText, Ticket ticket) {
        xhtmlText.appendOpenAnchorTag(configuration.getRtViewTicketUrl() + ticket.getId(), null);
        xhtmlText.append("#" + ticket.getId());
        xhtmlText.append(" - ");
        xhtmlText.append(ticket.getStringProperty("Subject"));
        xhtmlText.appendCloseAnchorTag();
    }

    @Override
    public String getCacheName() {
        return "CreatedDate";
    }
}
