/**
 * Copyright: 2010 FreeCode AS
 * Project: trumpeter
 * Created: Aug 1, 2009
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; version 3.
 */
package no.freecode.trumpeter.xmpp;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.XHTMLManager;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * Utility class for manipulating XMPP-related objects.
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public abstract class XmppUtils {

    /**
     * Creates a new group chat {@link Message} based on valid XHTML. It will
     * create two entries: a stripped one for clients that don't support XHTML,
     * and an XHTML version for the rest of them.
     * 
     * @param chat An active group chat which is to receive the message.
     * @param xhtmlMessage
     *            An XHTML string, for example
     *              "<body><p style='font-size:large; color: red;'>Mmm... bacon.</p></body>".
     * @return 
     */
    public static Message createChatMessage(MultiUserChat chat, String xhtmlMessage) {
        Message message = chat.createMessage();
        message.setBody(xhtmlMessage.replaceAll("\\<.*?>",""));
        XHTMLManager.addBody(message, xhtmlMessage);
        return message;
    }
}
