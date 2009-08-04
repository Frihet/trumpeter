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

import java.util.Set;

import org.apache.log4j.Logger;
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
    public void run() {
        Set<String> watchlist = getWatchlist();
        
//        rtConnection.getTicket(id)
        // TODO: continue here...
        
    }

    @Override
    public String getCacheName() {
        return "CreatedDate";
    }
}
