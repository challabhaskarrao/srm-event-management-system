package com.srm.events.service;

import com.srm.events.service.impl.NotificationScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NotificationSchedulerTest {
    @Test
    void schedulerRequiresExplicitEnablementAndUsesConfiguredSchedules() throws NoSuchMethodException {
        ConditionalOnProperty condition = NotificationScheduler.class.getAnnotation(ConditionalOnProperty.class);
        assertNotNull(condition);
        assertEquals("notifications.weekly-followup.enabled", condition.name()[0]);
        assertEquals("true", condition.havingValue());

        Scheduled weekly = NotificationScheduler.class.getMethod("weeklyMentorFollowUp").getAnnotation(Scheduled.class);
        Scheduled postEvent = NotificationScheduler.class.getMethod("postEventReminders").getAnnotation(Scheduled.class);
        assertNotNull(weekly);
        assertNotNull(postEvent);
        assertEquals("${notifications.weekly-followup.cron:0 0 9 ? * MON}", weekly.cron());
        assertEquals("${notifications.post-event.cron:0 15 * * * *}", postEvent.cron());
        assertEquals("${notifications.weekly-followup.timezone:Asia/Kolkata}", weekly.zone());
        assertEquals("${notifications.weekly-followup.timezone:Asia/Kolkata}", postEvent.zone());
    }
}
