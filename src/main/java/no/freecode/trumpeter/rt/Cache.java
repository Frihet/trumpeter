/**
 * Copyright: 2009 FreeCode AS
 * Project: trumpeter
 * Created: Jul 8, 2009
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; version 3.
 */
package no.freecode.trumpeter.rt;

import no.freecode.trumpeter.Configuration;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract {@link Rule} meant to be used by rules that are to use caching.
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public abstract class Cache {

	private static final Logger logger = Logger.getLogger(Cache.class);

	@Autowired
	protected Configuration configuration;
	
	private JCS cache;

    private String cacheRegion;


    /**
     * This method needs to return a string that identifies the cache that you
     * want to use. It will be prepended with a namespace to ensure that no
     * agents will operate on the same data.
     */
    public abstract String getCacheName();


    /**
	 * Get the {@link RuleCache} for a given ticket. If it hasn't been cached, a
	 * new instance will be created.
	 * 
	 * @param ticket
	 * @return
	 */
	public RuleCache getRuleCache(Ticket ticket) {
		RuleCache ruleCache = (RuleCache) getCache().get(ticket.getId());
		if (ruleCache == null) {
			ruleCache = new RuleCache();
		}
		return ruleCache;
	}

	/**
	 * Save the {@link RuleCache}.
	 * 
	 * @param ruleCache
	 * @param ticket
	 * @throws CacheException
	 */
	public void saveRuleCache(RuleCache ruleCache, Ticket ticket) {
		try {
		    getCache().put(ticket.getId(), ruleCache);
		} catch (CacheException e) {
			logger.fatal("Unable to save to cache.", e);
		}
	}

    /**
     * Set the caching namespace to use. This ensures that no two agents operate
     * on the same data.
     * 
	 * @param namespace A namespace String, e.g. "agent1".
	 */
	public void setNamespace(String namespace) {
	    this.cacheRegion = "rules-" + namespace + "-" + getCacheName();
	}

    private JCS getCache() {
        // Initialize the rule cache if it hasn't already been created.
        if (this.cache == null) {
            try {
                this.cache = JCS.getInstance(this.cacheRegion);
            } catch (CacheException e) {
                logger.fatal("Unable to initialize cache.", e);
            }
        }
        
        return cache;
    }
}
