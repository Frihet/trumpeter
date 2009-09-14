/**
 *  Project: trumpeter
 *  Created: Jul 4, 2009
 *  Copyright: 2009, Reidar Øksnevad
 *
 *  This file is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published
 *  by the Free Software Foundation; version 3.
 */
package no.freecode.trumpeter.xmpp;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

/**
 * This is the central class that manages communication with the XMPP server.
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public class XmppManager {

    private static final Logger logger = Logger.getLogger(XmppManager.class);
    
    private String username;
    private String password;
    private String resource;
    private String statusMessage = "";
    private String greeting;

    private boolean invokeOnStartup;
    private boolean sendPresence;

    private XMPPConnection connection;
    private XmppChatAgent[] agents;
    

    /**
     * Connect to an XMPP (Jabber) server.
     * 
     * @throws XMPPException
     *             if it isn't able to connect, or if there is an error joining
     *             a chat room.
     */
    public void connect() throws XMPPException {
        connection.connect();

        prepareChatAgent();
        
        connection.addConnectionListener(new ConnectionListener() {
            
            @Override
            public void reconnectionSuccessful() {
                logger.info("Successfully reconnected to the XMPP server.");
                try {
                    // Log on and rejoin the chats if the connection is lost.
                    prepareChatAgent();
                    
                } catch (XMPPException e) {
                    logger.error("Error preparing chat agent: " + e.getMessage());
                }
            }
            
            @Override
            public void reconnectionFailed(Exception arg0) {
                logger.info("Failed to reconnect to the XMPP server.");
            }

            @Override
            public void reconnectingIn(int seconds) {
                // logger.info("Reconnecting in " + seconds + " seconds.");
            }

            @Override
            public void connectionClosedOnError(Exception arg0) {
                logger.error("Connection to XMPP server was lost.");
            }
            
            @Override
            public void connectionClosed() {
                logger.info("XMPP connection was closed.");
            }
        });
    }

    @PreDestroy
    public void disconnect() {
        System.out.println("Disconnecting.");
        this.connection.disconnect();
    }
    
    private void prepareChatAgent() throws XMPPException {
        connection.login(getUsername(), getPassword(), getResource());
        logger.info("Connected to XMPP server: " + connection.getUser());
        
        // Send a status message if the user configured it to (default is no).
        if (isSendPresence()) {
            connection.sendPacket(new Presence(Presence.Type.available, getStatusMessage(), 0, Presence.Mode.available));
        }

        // Join chats.
        for (XmppChatAgent agent : getAgents()) {
            agent.joinChat();

            String greeting = getGreeting();
            if (greeting != null) {
                agent.sendMessage(greeting);
            }
        }
    }

    /**
     * Run whatever actions the agents are to do. This is called on a regular
     * basis, reading from RT and posting to the XMPP server.
     */
    public void invokeAgents() {
        for (XmppChatAgent agent : getAgents()) {
            agent.invoke();
        }
    }
    
    public XmppChatAgent[] getAgents() {
        return agents;
    }
    
    @Required
    @Autowired
    public void setAgents(XmppChatAgent[] agents) {
        this.agents = agents;
    }
    
    public String getUsername() {
        return username;
    }

    @Required
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    @Required
    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isInvokeOnStartup() {
        return invokeOnStartup;
    }

    public void setInvokeOnStartup(boolean invokeOnStartup) {
        this.invokeOnStartup = invokeOnStartup;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public XMPPConnection getConnection() {
        return connection;
    }

    @Required
    @Autowired
    public void setConnection(XMPPConnection connection) {
        this.connection = connection;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public boolean isSendPresence() {
        return sendPresence;
    }

    public void setSendPresence(boolean sendPresence) {
        this.sendPresence = sendPresence;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }
}
