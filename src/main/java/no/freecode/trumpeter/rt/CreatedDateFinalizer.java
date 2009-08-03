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

import org.apache.log4j.Logger;

/**
 * A {@link Rule} that examines the creation date of a ticket, and compares it
 * to a configurable date.
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public class CreatedDateFinalizer extends Cache implements Finalizer {

	private static final Logger logger = Logger.getLogger(CreatedDateFinalizer.class);


    @Override
    public void run() {
        
        
        
        // TODO do something...
        
    }

    @Override
    public String getCacheName() {
        return "CreatedDate";
    }
}
