package com.finpilot.service;

public interface EmiReminderService {

    /** Sends a reminder notification for every active loan whose next EMI is due within 3 days. */
    void sendUpcomingEmiReminders();
}