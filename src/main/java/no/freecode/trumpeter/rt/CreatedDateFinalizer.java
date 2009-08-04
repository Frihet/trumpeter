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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import no.freecode.trumpeter.rt.Ticket.Status;

import org.apache.log4j.Logger;
import org.jivesoftware.smackx.XHTMLText;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A {@link Rule} that examines the creation date of a ticket, and compares it
 * to a configurable date.
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public class CreatedDateFinalizer extends Cache implements Finalizer {

	private static final Logger logger = Logger.getLogger(CreatedDateFinalizer.class);

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

                            // TODO: use the same type of caching as in
                            // CreatedDateRule and handle e.g. stalled differently
                            // (all this needs to be more configurable).

                            removeFromWatchlist.add(ticketId); // don't talk about this ticket any more...
                        }

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
