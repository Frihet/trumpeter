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

import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;
import no.freecode.trumpeter.Configuration;
import no.freecode.trumpeter.rt.CreatedDateRule;
import no.freecode.trumpeter.rt.RtParser;
import no.freecode.trumpeter.rt.Ticket;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public class TestCreatedDateRule extends TestCase {

	public void testGetLastAcceptableDate() throws Exception {
        InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream("rt_output_1.txt");

        RtParser rtParser = new RtParser();
        rtParser.setConfiguration(new Configuration());
        List<Ticket> tickets = rtParser.parseTicketStream(stream);
        assertEquals(27, tickets.size());

        for (Ticket t : tickets) {
            assertTrue("Expected to find a TimeLeft key in ticket: " + ReflectionToStringBuilder.toString(t),
                    t.getTicketData().containsKey("TimeLeft"));
        }

        assertTrue(tickets.get(0).getCreatedDate().toString().contains("2009"));
        assertEquals("2327", tickets.get(0).getId());

        CreatedDateRule rule = new CreatedDateRule();
        rule.configuration = new Configuration();
//        System.out.println(rule.getMessage(tickets.get(0)));
	}
}
