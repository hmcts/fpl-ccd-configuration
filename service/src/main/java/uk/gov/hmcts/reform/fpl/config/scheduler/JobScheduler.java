package uk.gov.hmcts.reform.fpl.config.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true")
public class JobScheduler {

    private final Scheduler scheduler;
    private final SchedulerConfiguration schedulerConfig;

    @PostConstruct
    public void scheduleCronJobs() {
        log.info("Deleting jobs");

        schedulerConfig.getSchedulerConf()
            .stream()
            .map(SchedulerConfiguration.Schedule::getCronGroup)
            .distinct()
            .forEach(this::cleanJobs);

        log.info("Scheduling jobs");

        schedulerConfig.getSchedulerConf()
            .stream()
            .filter(SchedulerConfiguration.Schedule::isEnabled)
            .forEach(this::scheduleJob);

        log.info("Jobs scheduled");
    }

    private void scheduleJob(SchedulerConfiguration.Schedule jobData) {
        try {
            scheduler.scheduleJob(getJobDetail(jobData), getTrigger(jobData));
            log.info("Job '{}' scheduled", jobData.getName());
        } catch (SchedulerException e) {
            throw new UncheckedSchedulerException(e);
        }
    }

    private void cleanJobs(String scheduleGroup) {
        try {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.groupEquals(scheduleGroup))) {
                scheduler.deleteJob(jobKey);
                log.info("Job '{}' deleted", jobKey.getName());
            }
        } catch (SchedulerException e) {
            throw new UncheckedSchedulerException(e);
        }
    }

    private static JobDetail getJobDetail(SchedulerConfiguration.Schedule schedule) {
        return newJob(schedule.getJobClass())
            .withIdentity(schedule.getName(), schedule.getCronGroup())
            .withDescription(schedule.getDescription())
            .requestRecovery()
            .build();
    }

    private static Trigger getTrigger(SchedulerConfiguration.Schedule schedule) {
        return newTrigger()
            .withIdentity(schedule.getName(), schedule.getCronGroup())
            .withDescription(schedule.getDescription())
            .withSchedule(cronSchedule(schedule.getCron()))
            .build();
    }
}
