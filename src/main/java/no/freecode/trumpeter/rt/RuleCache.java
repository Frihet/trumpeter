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

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/**
 * A cache class that may be used by {@link Rule} implementations to save its actions (per ticket).
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public class RuleCache implements Serializable {

	private static final long serialVersionUID = 1L;

	private Set<String> handled;
	
	public RuleCache() { }


	public Set<String> getHandled() {
		if (handled == null) {
			handled = new TreeSet<String>();
		}
		return handled;
	}

	public void setHandled(Set<String> handled) {
		this.handled = handled;
	}
}
