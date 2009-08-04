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

import java.util.List;

/**
 * Interface for all RT finalizers.
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public interface Finalizer {

    List<String> getMessages();

}
