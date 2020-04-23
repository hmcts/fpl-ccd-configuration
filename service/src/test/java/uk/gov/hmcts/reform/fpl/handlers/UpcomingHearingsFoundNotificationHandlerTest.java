package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.UpcomingHearingsFound;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.UpcomingHearingsContentProvider;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UPCOMING_HEARINGS_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {UpcomingHearingsFoundNotificationHandler.class, LookupTestConfig.class})
public class UpcomingHearingsFoundNotificationHandlerTest {
    @MockBean
    private NotificationService notificationService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private UpcomingHearingsContentProvider upcomingHearingsEmailContentProvider;

    @Autowired
    private UpcomingHearingsFoundNotificationHandler upcomingHearingsFoundNotificationHandler;

    LocalDate hearingDate = LocalDate.now();

    @Test
    void shouldSendEmailWithUpcomingHearings() {
        final List<CaseDetails> cases = List.of(CaseDetails.builder().build());
        final Map<String, Object> params = Map.of("testKey", "testValue");
        final UpcomingHearingsFound upcomingHearings = new UpcomingHearingsFound(hearingDate, cases);

        when(featureToggleService.isCtscReportEnabled()).thenReturn(true);
        when(upcomingHearingsEmailContentProvider.buildParameters(hearingDate, cases))
            .thenReturn(params);

        upcomingHearingsFoundNotificationHandler.sendEmailWithUpcomingHearings(upcomingHearings);

        verify(notificationService).sendEmail(UPCOMING_HEARINGS_TEMPLATE, CTSC_INBOX, params,
            hearingDate.toString());
    }

    @Test
    void shouldNotSendEmailWhenCtscNotificationsTurnedOff() {
        List<CaseDetails> cases = List.of(CaseDetails.builder().build());
        UpcomingHearingsFound upcomingHearings = new UpcomingHearingsFound(hearingDate, cases);

        when(featureToggleService.isCtscReportEnabled()).thenReturn(false);

        upcomingHearingsFoundNotificationHandler.sendEmailWithUpcomingHearings(upcomingHearings);

        verify(notificationService, never()).sendEmail(any(), any(), anyMap(), any());
    }

    @Test
    void shouldNotSendEmailWhenNoCasesToBeHeard() {
        List<CaseDetails> cases = emptyList();
        UpcomingHearingsFound upcomingHearings = new UpcomingHearingsFound(hearingDate, cases);

        when(featureToggleService.isCtscReportEnabled()).thenReturn(true);

        upcomingHearingsFoundNotificationHandler.sendEmailWithUpcomingHearings(upcomingHearings);

        verify(notificationService, never()).sendEmail(any(), any(), anyMap(), any());
    }
}
