/**
 * Copyright: 2010 FreeCode AS
 * Project: trumpeter
 * Created: Nov 1, 2010
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; version 3.
 */
package no.freecode.trumpeter.xmpp;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.ajp.Ajp13SocketConnector;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.AbstractHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Embedded a Jetty HTTP server, with AJP support.
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public class HttpServerAgent extends XmppChatAgent {

    private static final Logger logger = Logger.getLogger(HttpServerAgent.class);


    private Integer tcpPort;

    private Integer ajpPort;

    
	/**
	 * This is called when the dependency injection is complete, and starts up
	 * the server, listening for input to send to the XMPP chat.
	 */
    @PostConstruct
    public void startHttpServer() {
        Handler handler = new AbstractHandler() {
            public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
                	throws IOException, ServletException {

            	response.setContentType("text/plain");
            	response.setStatus(HttpServletResponse.SC_OK);

            	String message = request.getParameter("message");

            	if (StringUtils.isNotBlank(message)) {
            		logger.info("Sending message to chat window: '" + message + "'");
            		sendMessage(XmppUtils.createChatMessage(chat, message));
            		response.getWriter().println("Thanks. Your message was sent to the chat window.");

            	} else {
            		response.getWriter().println("Your message is empty. Nothing was sent to the chat. Hint: http://.../?message=...");
            	}
                
                ((Request) request).setHandled(true);
            }
        };

        Server server = new Server();
        server.setHandler(handler);

        if (this.tcpPort != null) {
        	Connector tcpConnector = new SocketConnector();
        	tcpConnector.setPort(this.tcpPort);
        	server.addConnector(tcpConnector);
        }
        
        if (this.ajpPort != null) {
        	Connector ajpConnector = new Ajp13SocketConnector();
        	ajpConnector.setPort(this.ajpPort);
        	ajpConnector.setHost("127.0.0.1");
        	server.addConnector(ajpConnector);
        }

        try {
			server.start();
		} catch (Exception e) {
			logger.error("Unable to start the HTTP server. The error is: " + e.getMessage());
		}
    }
	
    /**
	 * The port to bind to if we're serving the pages using regular TCP (HTTP).
	 * E.g. 8080. If you don't set a value, it will not be used.
	 */
    @Autowired(required = false)
    public void setTcpPort(Integer tcpPort) {
		this.tcpPort = tcpPort;
	}
    
	/**
	 * The port to bind to if we're using AJP to serve the pages. 8009 is a
	 * common port for AJP. If you don't set a value, it will not be used.
     */
    @Autowired(required = false)
    public void setAjpPort(Integer ajpPort) {
		this.ajpPort = ajpPort;
	}
    
    
    public static void main(String[] args) throws InterruptedException {
		HttpServerAgent agent = new HttpServerAgent();
		agent.setAjpPort(8009);
		agent.setTcpPort(8080);
		agent.startHttpServer();
		Thread.sleep(1000 * 60 * 60);
	}
    
	@Override
	public void invoke() {
		// This agent should run its actions continuously, so this method is not really needed.
	}

}
