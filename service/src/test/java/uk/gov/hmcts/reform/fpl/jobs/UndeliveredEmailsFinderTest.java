package uk.gov.hmcts.reform.fpl.jobs;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.fpl.events.UndeliveredEmailsFound;
import uk.gov.hmcts.reform.fpl.exceptions.JobException;
import uk.gov.hmcts.reform.fpl.model.UndeliveredEmail;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogger;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogs;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogsExtension;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationList;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, TestLogsExtension.class})
class UndeliveredEmailsFinderTest {

    @Mock
    private JobKey jobKey;

    @Mock
    private JobDetail jobDetail;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @Mock
    private Notification notification1;

    @Mock
    private Notification notification2;

    @Mock
    private NotificationList notificationList;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @TestLogs
    private TestLogger logs = new TestLogger(UndeliveredEmailsFinder.class);

    @InjectMocks
    private UndeliveredEmailsFinder underTest;

    @BeforeEach
    void init() throws Exception {
        when(jobKey.getName()).thenReturn("testName");
        when(jobDetail.getKey()).thenReturn(jobKey);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);

        when(notificationClient.getNotifications("failed", "email", null, null)).thenReturn(notificationList);
    }

    @Test
    void shouldEmitUndeliveredEmailsFoundEvent() {
        when(notificationList.getNotifications()).thenReturn(List.of(notification1, notification2));

        when(notification1.getCompletedAt()).thenReturn(Optional.of(ZonedDateTime.now().minusHours(23)));
        when(notification1.getEmailAddress()).thenReturn(Optional.of("test@test.com"));
        when(notification1.getSubject()).thenReturn(Optional.of("Subject"));
        when(notification1.getReference()).thenReturn(Optional.of("Reference"));

        when(notification2.getCompletedAt()).thenReturn(Optional.of(ZonedDateTime.now().minusHours(10)));
        when(notification2.getEmailAddress()).thenReturn(Optional.of("test2@test.com"));
        when(notification2.getSubject()).thenReturn(Optional.of("Subject 2"));
        when(notification2.getReference()).thenReturn(Optional.of("Reference 2"));

        underTest.execute(jobExecutionContext);

        List<UndeliveredEmail> undeliveredEmails = List.of(
            UndeliveredEmail.builder()
                .recipient("test@test.com")
                .subject("Subject")
                .reference("Reference")
                .build(),
            UndeliveredEmail.builder()
                .recipient("test2@test.com")
                .subject("Subject 2")
                .reference("Reference 2")
                .build()
        );

        verify(applicationEventPublisher).publishEvent(new UndeliveredEmailsFound(undeliveredEmails));
    }

    @Test
    void shouldEmitUndeliveredEmailsFoundEventWhenEmailFailedWithinLastDay() {
        when(notificationList.getNotifications()).thenReturn(List.of(notification1));

        when(notification1.getCompletedAt()).thenReturn(Optional.of(ZonedDateTime.now().minusDays(1).plusHours(1)));
        when(notification1.getEmailAddress()).thenReturn(Optional.of("test@test.com"));
        when(notification1.getSubject()).thenReturn(Optional.of("Subject"));
        when(notification1.getReference()).thenReturn(Optional.of("Reference"));

        underTest.execute(jobExecutionContext);

        List<UndeliveredEmail> undeliveredEmails = List.of(UndeliveredEmail.builder()
            .recipient("test@test.com")
            .subject("Subject")
            .reference("Reference")
            .build());

        verify(applicationEventPublisher).publishEvent(new UndeliveredEmailsFound(undeliveredEmails));
    }

    @Test
    void shouldIgnoreEmailsThatFailedEarlierThanDayAgo() {
        when(notificationList.getNotifications()).thenReturn(List.of(notification1));

        when(notification1.getCompletedAt()).thenReturn(Optional.of(ZonedDateTime.now().minusDays(1).minusHours(1)));

        underTest.execute(jobExecutionContext);

        verify(applicationEventPublisher, never()).publishEvent(any());

        assertThat(logs.get()).contains("Job 'testName' did not find any undelivered emails");
    }

    @Test
    void shouldIgnoreEmailsThatFailedButDidNotReachMaxDeliveryAttempts() {
        when(notificationList.getNotifications()).thenReturn(List.of(notification1));

        when(notification1.getCompletedAt()).thenReturn(Optional.empty());

        underTest.execute(jobExecutionContext);

        verify(applicationEventPublisher, never()).publishEvent(any());

        assertThat(logs.get()).contains("Job 'testName' did not find any undelivered emails");
    }

    @Test
    void shouldLogFailureWhenUnexpectedExceptionThrown() {
        final Exception exception = new RuntimeException("Test");

        when(notificationList.getNotifications()).thenThrow(exception);

        assertThatThrownBy(() -> underTest.execute(jobExecutionContext))
            .isInstanceOf(JobException.class)
            .hasRootCause(exception);

        verify(applicationEventPublisher, never()).publishEvent(any());

        assertThat(logs.get()).doesNotContain("Job 'testName' finished");
    }
}
