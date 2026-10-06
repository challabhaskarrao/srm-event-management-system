package com.srm.events.service.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="notifications.weekly-followup.enabled", havingValue="true")
public class NotificationScheduler {
    private final NotificationGenerationService generation;
    public NotificationScheduler(NotificationGenerationService generation) { this.generation=generation; }
    @Scheduled(cron="${notifications.weekly-followup.cron:0 0 9 ? * MON}", zone="${notifications.weekly-followup.timezone:Asia/Kolkata}")
    public void weeklyMentorFollowUp() { generation.generateWeeklyMentorFollowUps(java.time.LocalDate.now()); }
    @Scheduled(cron="${notifications.post-event.cron:0 15 * * * *}", zone="${notifications.weekly-followup.timezone:Asia/Kolkata}")
    public void postEventReminders() { generation.generatePostEventReminders(); }
}
