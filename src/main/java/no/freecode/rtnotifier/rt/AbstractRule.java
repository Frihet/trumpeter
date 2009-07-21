/**
 * Copyright: 2009 FreeCode AS
 * Project: rtnotifier
 * Created: Jul 8, 2009
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; version 3.
 */
package no.freecode.rtnotifier.rt;

import no.freecode.rtnotifier.Configuration;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 *
 */
public abstract class AbstractRule implements Rule {

	private static final Logger logger = Logger.getLogger(AbstractRule.class);
	
	@Autowired
	protected Configuration configuration;
	
	private JCS cache;

	/**
	 * 
	 * @throws CacheException if the cache cannot be created.
	 */
	public AbstractRule() {
		try {
			// Initialize the route cache.
			this.cache = JCS.getInstance("rules");
		} catch (CacheException e) {
			logger.fatal("Unable to initialize cache.", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see no.freecode.rtnotifier.rt.Rule#getMessage(no.freecode.rtnotifier.rt.Ticket)
	 */
	public abstract String getMessage(Ticket ticket);

	/**
	 * Get the {@link RuleCache} for a given ticket. If it hasn't been cached, a
	 * new instance will be created.
	 * 
	 * @param ticket
	 * @return
	 */
	public RuleCache getRuleCache(Ticket ticket) {
		RuleCache ruleCache = (RuleCache) this.cache.get(ticket.getId());
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
			this.cache.put(ticket.getId(), ruleCache);
		} catch (CacheException e) {
			logger.fatal("Unable to save to cache.", e);
		}
	}
}
