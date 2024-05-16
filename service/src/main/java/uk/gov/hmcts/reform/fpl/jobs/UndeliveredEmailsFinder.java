package uk.gov.hmcts.reform.fpl.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.fpl.events.UndeliveredEmailsFound;
import uk.gov.hmcts.reform.fpl.exceptions.JobException;
import uk.gov.hmcts.reform.fpl.model.UndeliveredEmail;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationList;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true")
public class UndeliveredEmailsFinder implements Job {

    private static final int REPORT_PERIOD_IN_DAYS = 1;

    private final NotificationClient notifications;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        final String jobName = jobExecutionContext.getJobDetail().getKey().getName();

        log.info("Job '{}' started", jobName);

        try {
            DateTime reportFrom = DateTime.now().minusDays(REPORT_PERIOD_IN_DAYS);

            NotificationList undeliveredEmailsReport = notifications.getNotifications("failed", "email", null, null);

            List<UndeliveredEmail> undeliveredEmails = undeliveredEmailsReport.getNotifications().stream()
                .filter(email -> email.getCompletedAt().isPresent())
                .filter(email -> email.getCompletedAt().get().isAfter(reportFrom))
                .map(UndeliveredEmail::fromNotification)
                .collect(toList());

            if (undeliveredEmails.isEmpty()) {
                log.info("Job '{}' did not find any undelivered emails", jobName);
            } else {
                log.info("Job '{}' found {} undelivered email(s)", jobName, undeliveredEmails.size());
                applicationEventPublisher.publishEvent(new UndeliveredEmailsFound(undeliveredEmails));
            }

        } catch (Exception ex) {
            throw new JobException(jobName, ex);
        }

        log.info("Job '{}' finished", jobName);
    }
}
