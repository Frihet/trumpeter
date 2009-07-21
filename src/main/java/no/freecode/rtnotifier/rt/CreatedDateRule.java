/**
 * Copyright: 2009 FreeCode AS
 * Project: rtnotifier
 * Created: Jul 5, 2009
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; version 3.
 */
package no.freecode.rtnotifier.rt;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smackx.XHTMLText;

/**
 * A {@link Rule} that examines the creation date of a ticket, and compares it
 * to a configurable date.
 * 
 * @author Reidar Øksnevad (reidar.oksnevad@freecode.no)
 */
public class CreatedDateRule extends AbstractRule {

	private static final Logger logger = Logger.getLogger(CreatedDateRule.class);

	private int workdayBeginsAt = 9;
	private int workdayEndsAt = 16;
	private int maxAgeInHours = 24;
	private int workHourWarningInHours = 2;
	
	private String slaWarningComment = "";
	private String slaBreachComment = "Please handle immediately!";
	
	private static final String NEW_MESSAGE_SENT = "new_sent";
	private static final String SLA_WARNING_SENT = "warning_sent";
	private static final String SLA_BREACH_SENT = "breach_sent";


	public int getMaxAgeInHours() {
		return maxAgeInHours;
	}

	/**
	 * Set the maximum age (in hours) of a ticket's creation before the
	 * rule evaluates to <code>false</code>.
	 * 
	 * @param maxAgeInHours
	 */
	public void setMaxAgeInHours(int maxAgeInHours) {
		this.maxAgeInHours = maxAgeInHours;
	}

	/**
	 * @return the workdayBeginsAt
	 */
	public int getWorkdayBeginsAt() {
		return workdayBeginsAt;
	}

	/**
	 * @param workdayBeginsAt the workdayBeginsAt to set
	 */
	public void setWorkdayBeginsAt(int workdayBeginsAt) {
		this.workdayBeginsAt = workdayBeginsAt;
	}

	/**
	 * @return the workdayEndsAt
	 */
	public int getWorkdayEndsAt() {
		return workdayEndsAt;
	}

	/**
	 * @return the workHourWarningInHours
	 */
	public int getWorkHourWarningInHours() {
		return workHourWarningInHours;
	}

	/**
	 * @param workHourWarningInHours the workHourWarningInHours to set
	 */
	public void setWorkHourWarningInHours(int workHourWarningInHours) {
		this.workHourWarningInHours = workHourWarningInHours;
	}

	/**
	 * @param workdayEndsAt the workdayEndsAt to set
	 */
	public void setWorkdayEndsAt(int workdayEndsAt) {
		this.workdayEndsAt = workdayEndsAt;
	}
	
	public String getSlaWarningComment() {
		return slaWarningComment;
	}

	public void setSlaWarningComment(String slaWarningComment) {
		this.slaWarningComment = slaWarningComment;
	}

	public String getSlaBreachComment() {
		return slaBreachComment;
	}

	public void setSlaBreachComment(String slaBreachComment) {
		this.slaBreachComment = slaBreachComment;
	}

	/**
	 * Determine whether the {@link Calendar} time is a work hour or not.
	 */
	private boolean isWorkHour(Calendar cal) {
		int day = cal.get(Calendar.DAY_OF_WEEK);
		if (day != Calendar.SATURDAY && day != Calendar.SUNDAY) {
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			if (hour >= getWorkdayBeginsAt() && hour < getWorkdayEndsAt()) {
				return true;
			}
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see no.freecode.rtnotifier.rt.AbstractRule#getMessage(no.freecode.rtnotifier.rt.Ticket)
	 */
	@Override
	public String getMessage(Ticket ticket) {
		
		Calendar cal = new GregorianCalendar();
		cal.setTime(ticket.getCreatedDate());

		// Keep moving forward until we find the first working hour.
		while (!isWorkHour(cal)) {
			cal.add(Calendar.HOUR_OF_DAY, 1);
			cal.set(Calendar.MINUTE, 0); // XXX: don't need to call this each time we loop, but it's more readable ;)
		}

//		Date calculatedStartTime = DateUtils.truncate(cal.getTime(), Calendar.MINUTE);

		cal.add(Calendar.HOUR_OF_DAY, getMaxAgeInHours());
		while (!isWorkHour(cal)) {
			cal.add(Calendar.HOUR_OF_DAY, 1);
			cal.set(Calendar.MINUTE, 0);
		}

		Date calculatedDueTime = DateUtils.truncate(cal.getTime(), Calendar.MINUTE);
//		System.out.println("due time @ " + calculatedDueTime);

		// Move backwards the configured number of work hours, and compare with
		// current time.
		for (int i = getWorkHourWarningInHours(); i > 0; i--) {
			cal.add(Calendar.HOUR_OF_DAY, -1);
			while (!isWorkHour(cal)) {
				cal.add(Calendar.HOUR_OF_DAY, -1);
			}
		}

		Date calculatedWarningTime = DateUtils.truncate(cal.getTime(), Calendar.MINUTE);

		RuleCache ruleCache = getRuleCache(ticket);
		Set<String> handled = ruleCache.getHandled();
		String resultMessage = null;  // null is ok ;)

		// Return messages if there is anything relevant to say.
		Date now = new Date();
		if (!handled.contains(SLA_BREACH_SENT) && now.after(calculatedDueTime)) {
			XHTMLText xhtmlText = new XHTMLText(null, null);
			xhtmlText.appendOpenStrongTag();
			xhtmlText.appendOpenSpanTag("color: red");
			xhtmlText.append("SLA breach (" + getMaxAgeInHours() + "h): ");
			xhtmlText.appendCloseSpanTag();
			xhtmlText.appendCloseStrongTag();
			xhtmlText.append("issue ");
			appendTicketDescription(xhtmlText, ticket);
			xhtmlText.append(". " + getSlaBreachComment());
			resultMessage = xhtmlText.toString();
			handled.add(SLA_BREACH_SENT);
			handled.add(SLA_WARNING_SENT);
			handled.add(NEW_MESSAGE_SENT);

		} else if (!handled.contains(SLA_WARNING_SENT) && now.after(calculatedWarningTime)) {
			XHTMLText xhtmlText = new XHTMLText(null, null);
			xhtmlText.appendOpenStrongTag();
			xhtmlText.append("Warning: ");
			xhtmlText.appendCloseStrongTag();
			xhtmlText.append("issue ");
			appendTicketDescription(xhtmlText, ticket);
			xhtmlText.append(". Please handle before "
					+ DateFormatUtils.format(calculatedDueTime, "yyyy-MM-dd HH:mm") + ". "
					+ getSlaWarningComment());
			resultMessage = xhtmlText.toString();
			handled.add(SLA_WARNING_SENT);
			handled.add(NEW_MESSAGE_SENT);

		} else if (!handled.contains(NEW_MESSAGE_SENT)) {
			XHTMLText xhtmlText = new XHTMLText(null, null);
			xhtmlText.appendOpenStrongTag();
			xhtmlText.append("New issue: ");
			xhtmlText.appendCloseStrongTag();
			appendTicketDescription(xhtmlText, ticket);
			resultMessage = xhtmlText.toString();
			handled.add(NEW_MESSAGE_SENT);
		}

		// Update cache so that we don't send the same message twice.
		saveRuleCache(ruleCache, ticket);

		return resultMessage;
	}

	/**
	 * Add a ticket description / link to an {@link XHTMLText}.
	 */
	private void appendTicketDescription(XHTMLText xhtmlText, Ticket ticket) {
		xhtmlText.appendOpenAnchorTag(configuration.getRtViewTicketUrl() + ticket.getId(), null);
		xhtmlText.append("#" + ticket.getId());
		xhtmlText.append(" - ");
		xhtmlText.append(ticket.getStringProperty("Subject"));
		xhtmlText.appendCloseAnchorTag();
	}
}
