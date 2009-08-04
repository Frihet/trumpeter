/**
 *  Project: trumpeter
 *  Created: Jul 4, 2009
 *  Copyright: 2009, Reidar Øksnevad
 *
 *  This file is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published
 *  by the Free Software Foundation; version 3.
 */
package no.freecode.trumpeter.rt;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import no.freecode.trumpeter.Configuration;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

/**
 * Used for communicating with RT over REST.
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public class RtConnection {

    private static final Logger logger = Logger.getLogger(RtConnection.class);

    private Configuration configuration;

    @Autowired
    private RtParser rtParser;

    private String username;
    private String password;
    private boolean useBasicAuthentication;
    
    private HttpClient client;

    /**
     * Set up the http client with basic auth stuff ++.
     * 
     * @throws IOException
     * @throws HttpException
     * @throws BeanInitializationException
     */
    @PostConstruct
    public void init() throws BeanInitializationException, HttpException, IOException {
        this.client = new HttpClient();

        if (isUseBasicAuthentication()) {
            // Prepare for basic authentication.
            this.client.getState().setCredentials(new AuthScope(null, 443, null),
                    new UsernamePasswordCredentials(getUsername(), getPassword()));
        }

        verifyRt();
    }

    /**
     * Make sure RT is reachable before starting up anything else.
     * 
     * @throws IOException
     * @throws HttpException
     */
    private void verifyRt() throws BeanInitializationException, HttpException, IOException {
        // Run a query that isn't supposed to return any results, just to make
        // sure RT is accessible. This is executed at application start time,
        // and the application shouldn't start if it doesn't work.
        getTickets("Queue='nonexistingqueue'");
    }

    /**
     * Get the tickets returned by an arbitrary RT query.
     * 
     * @param query
     *            A query that is expected to return RT tickets.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public List<Ticket> getTickets(String query) throws HttpException, IOException {
        return getTicketsImpl("/REST/1.0/search/ticket?format=l&query=" + query);
    }
    
    /**
     * Get a single ticket.
     * 
     * @param id Ticket id (e.g. "1234").
     * @return the ticket, or null if it doesn't exist.
     * 
     * @throws IOException 
     * @throws HttpException 
     */
    public Ticket getTicket(String id) throws HttpException, IOException {
        int intId = NumberUtils.toInt(id); // avoid potential injection issues
        List<Ticket> tickets = getTicketsImpl("/REST/1.0/ticket/" + intId + "/show");
        if (tickets.size() > 0) {
            return tickets.get(0);
        } else {
            return null;
        }
    }
    
    /**
     * @param urlString
     * @return
     * @throws HttpException
     * @throws IOException
     */
    private List<Ticket> getTicketsImpl(String urlString) throws HttpException, IOException {
        StringBuilder url = new StringBuilder(getConfiguration().getRtBaseUrl());
        url.append(urlString);

        if (!isUseBasicAuthentication()) {
            // If BasicAuth is not used, the username and password has to be included in the URL.
            url.append("&user=" + getUsername() + "&pass=" + getPassword());
        }

        GetMethod get = new GetMethod(url.toString());
        
        if (isUseBasicAuthentication()) {
            get.setDoAuthentication(true); // automatically handle authentication.
        }

        try {
            // Execute the HTTP GET
            client.executeMethod(get);

            // Return the tickets received from RT.
            return rtParser.parseTicketStream(get.getResponseBodyAsStream());

        } finally {
            // Release resources used by the GET method.
            get.releaseConnection();
        }
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

    /**
     * @return Whether or not HTTP Basic Authentication is used when
     *         communicating with RT.
     */
    public boolean isUseBasicAuthentication() {
        return useBasicAuthentication;
    }

    /**
     * Specify whether or not HTTP Basic Authentication should be used when
     * communicating with RT.
     */
    public void setUseBasicAuthentication(boolean useBasicAuthentication) {
        this.useBasicAuthentication = useBasicAuthentication;
    }

    /*
     * (non-Javadoc)
     * 
     * @see no.freecode.trumpeter.Agent#getConfiguration()
     */
    public Configuration getConfiguration() {
        return this.configuration;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeno.freecode.trumpeter.Agent#setConfiguration(no.freecode.trumpeter.
     * Configuration)
     */
    @Autowired
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
