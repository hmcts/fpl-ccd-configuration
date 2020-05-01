package uk.gov.hmcts.reform.fpl.jobs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.UpcomingHearingsFound;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.util.List;

import static java.time.LocalDate.now;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyList;
import static java.util.Date.from;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FixedTimeConfiguration.class})
public class UpcomingHearingsFinderTest {

    private static final int DEFAULT_NOTICE_DAYS_BEFORE_HEARING = 2;

    private LocalDate hearingDate;

    @Mock
    private CalendarService calendarService;

    @Mock
    private SearchService searchService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @Mock
    private JobDetail jobDetail;

    @Mock
    private JobKey jobKey;

    @Autowired
    Time time;

    @InjectMocks
    private UpcomingHearingsFinder upcomingHearingsFinder;

    private LocalDate today;

    @BeforeEach
    void init() {
        today = time.now().toLocalDate();
        hearingDate = now().plusDays(DEFAULT_NOTICE_DAYS_BEFORE_HEARING);
        when(jobKey.getName()).thenReturn("testName");
        when(jobDetail.getKey()).thenReturn(jobKey);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobExecutionContext.getScheduledFireTime()).thenReturn(from(today.atStartOfDay().toInstant(UTC)));
        when(calendarService.getWorkingDayFrom(today, DEFAULT_NOTICE_DAYS_BEFORE_HEARING)).thenReturn(hearingDate);
    }

    @Test
    void shouldEmitUpcomingHearingsFoundEventWhenCasesToBeHeardFound() {
        List<CaseDetails> caseDetails = List.of(CaseDetails.builder().build());

        when(calendarService.isWorkingDay(today)).thenReturn(true);
        when(searchService.search("data.hearingDetails.value.startDate", hearingDate)).thenReturn(caseDetails);

        upcomingHearingsFinder.execute(jobExecutionContext);

        verify(applicationEventPublisher).publishEvent(new UpcomingHearingsFound(hearingDate, caseDetails));
    }

    @Test
    void shouldNotEmitUpcomingHearingsFoundEventWhenCasesToBeHeardNotFound() {
        List<CaseDetails> caseDetails = emptyList();

        when(calendarService.isWorkingDay(today)).thenReturn(true);
        when(searchService.search("data.hearingDetails.value.startDate", hearingDate)).thenReturn(caseDetails);

        upcomingHearingsFinder.execute(jobExecutionContext);

        verifyNoMoreInteractions(applicationEventPublisher);
    }

    @Test
    void shouldNotEmitUpcomingHearingsFoundEventWhenBankHoliday() {
        when(calendarService.isWorkingDay(today)).thenReturn(false);

        upcomingHearingsFinder.execute(jobExecutionContext);

        verifyNoMoreInteractions(applicationEventPublisher);
    }
}
