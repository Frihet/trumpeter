/**
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; version 3.
 *
 * Copyright: 2009 FreeCode AS
 * Project: rtnotifier
 * Created: Jul 4, 2009
 */
package no.freecode.rtnotifier;


/**
 * General interface for all agents.
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public interface Agent {

    /**
     * Do whatever the agent is supposed to do.
     */
    void invoke();
    
    /**
     * Send a message to some sort of receiver.
     */
    void sendMessage(String message);
}
