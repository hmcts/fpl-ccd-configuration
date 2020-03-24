package uk.gov.hmcts.reform.fpl.jobs;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.UpcomingHearingsFound;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;

import java.time.LocalDate;
import java.util.List;

import static java.time.ZoneOffset.UTC;

@Slf4j
public class UpcomingHearingsFinder implements Job {

    @Value("${UPCOMING_HEARINGS_DAYS:2}")
    private int noticeDaysBeforeHearing = 2;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        final String jobName = jobExecutionContext.getJobDetail().getKey().getName();
        log.info("Job '{}' started", jobName);

        LocalDate baseDate = jobExecutionContext.getScheduledFireTime().toInstant().atZone(UTC).toLocalDate();

        LocalDate hearingDate = calendarService.getWorkingDayFrom(baseDate, noticeDaysBeforeHearing);
        List<CaseDetails> cases = searchService.search("data.hearingDetails.value.startDate", hearingDate);

        if (cases.isEmpty()) {
            log.info("Job '{}' did not find any cases", jobName);
        } else {
            log.info("Job '{}' found {} case(s)", jobName, cases.size());
            applicationEventPublisher.publishEvent(new UpcomingHearingsFound(hearingDate, cases));
        }

        log.info("Job '{}' finished", jobName);
    }
}
