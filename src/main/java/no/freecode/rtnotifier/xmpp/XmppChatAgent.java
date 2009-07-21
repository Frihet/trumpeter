/**
 * Copyright: 2009 FreeCode AS
 * Project: rtnotifier
 * Created: Jul 4, 2009
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; version 3.
 */
package no.freecode.rtnotifier.xmpp;

import no.freecode.rtnotifier.Agent;
import no.freecode.rtnotifier.Configuration;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 *
 */
public abstract class XmppChatAgent implements Agent {

	private static final Logger logger = Logger.getLogger(XmppChatAgent.class);

    private String alias;
    private String chatRoom;
    private XMPPConnection connection;

    @Autowired
    protected Configuration configuration;  // Global application configuration
    
    protected MultiUserChat chat;


    /**
     * When the connection is ready, this method may be called in order to join
     * the specified chat room.
     * 
     * @throws XMPPException if we're not able to join the chat room.
     */
    public void joinChat() throws XMPPException {
        // Create a MultiUserChat representing the chat room.
        this.chat = new MultiUserChat(getConnection(), getChatRoom());

        // Join the chat!
        this.chat.join(getAlias());

        System.out.println("Agent '" + getAlias() + "' joined chatroom '" + getChatRoom() + "'.");
    }

    public String getAlias() {
        return alias;
    }

    @Required
    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    public String getChatRoom() {
        return chatRoom;
    }

    @Required
    public void setChatRoom(String chatRoom) {
        this.chatRoom = chatRoom;
    }
    
    /* (non-Javadoc)
     * @see no.freecode.rtnotifier.Agent#sendMessage()
     */
    public void sendMessage(String message) {
    	try {
			this.chat.sendMessage(message);
		} catch (XMPPException e) {
			logger.error("Error sending message to the chat room.", e);
			// TODO: reconnect if the connection is lost?
		}
    }

    /* (non-Javadoc)
     * @see no.freecode.rtnotifier.Agent#sendMessage()
     */
    public void sendMessage(Message message) {
    	try {
			this.chat.sendMessage(message);
		} catch (XMPPException e) {
			logger.error("Error sending message to the chat room.", e);
			// TODO: reconnect if the connection is lost?
		}
    }

    
    public XMPPConnection getConnection() {
        return connection;
    }

    @Autowired
    public void setConnection(XMPPConnection connection) {
        this.connection = connection;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Agent[" + getAlias() + "]";
    }
}
