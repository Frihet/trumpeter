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

/**
 * Interface for all RT rules.
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public interface Rule {

	/**
	 * This method does not necessarily return a message. In the normal case, it
	 * will just return null, but when the Ticket does not pass the implemented
	 * rule (or there is information to be sent out), it will return a message
	 * which can be passed to a user in e.g. Jabber.
	 * 
	 * @param ticket An RT ticket.
	 * @return null if the ticket is ok, otherwise an error message.
	 */
    String getMessage(Ticket ticket);
}
