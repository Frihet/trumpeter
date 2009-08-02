/**
 * Copyright: 2009 FreeCode AS
 * Project: trumpeter
 * Created: Aug 2, 2009
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; version 3.
 */
package no.freecode.trumpeter.rt;

import java.util.HashMap;

import junit.framework.TestCase;

/**
 * Unit test for the {@link Ticket} class.
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public class TicketTest extends TestCase {

    private Ticket ticket;

    @Override
    protected void setUp() throws Exception {
        HashMap<String, String> ticketData = new HashMap<String, String>();
        ticketData.put("Created", "Sun Aug 02 21:32:37 2009");
        ticket = new Ticket(ticketData, 0);
    }

    @SuppressWarnings("deprecation")
    public void testNoHourOffset() {
        assertEquals(21, ticket.getCreatedDate().getHours());
    }

    @SuppressWarnings("deprecation")
    public void testHourOffsets() {
        ticket.setHourOffset(-2);
        assertEquals(19, ticket.getCreatedDate().getHours());
    }
}
