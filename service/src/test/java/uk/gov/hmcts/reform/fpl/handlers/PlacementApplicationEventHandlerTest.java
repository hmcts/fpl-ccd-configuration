package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.PlacementApplicationContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;

@ExtendWith(MockitoExtension.class)
class PlacementApplicationEventHandlerTest {
    private static final BaseCaseNotifyData NOTIFY_DATA = mock(BaseCaseNotifyData.class);
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final long CASE_ID = 123456L;

    @Mock
    private NotificationService notificationService;
    @Mock
    private CourtService courtService;
    @Mock
    private PlacementApplicationContentProvider contentProvider;
    @InjectMocks
    private PlacementApplicationEventHandler underTest;

    @Test
    void shouldNotifyHmctsAdminOfPlacementApplicationUploadWhenCtscIsDisabled() {
        given(contentProvider.buildPlacementApplicationNotificationParameters(CASE_DATA)).willReturn(NOTIFY_DATA);
        given(courtService.getCourtEmail(CASE_DATA)).willReturn(COURT_EMAIL_ADDRESS);
        given(CASE_DATA.getId()).willReturn(CASE_ID);

        underTest.notifyAdmin(new PlacementApplicationEvent(CASE_DATA));

        verify(notificationService).sendEmail(
            PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE, COURT_EMAIL_ADDRESS, NOTIFY_DATA, CASE_ID
        );
    }

    @Test
    void shouldNotifyCtscAdminOfPlacementApplicationUploadWhenCtscIsEnabled() {
        given(contentProvider.buildPlacementApplicationNotificationParameters(CASE_DATA)).willReturn(NOTIFY_DATA);
        given(courtService.getCourtEmail(CASE_DATA)).willReturn(CTSC_INBOX);
        given(CASE_DATA.getId()).willReturn(CASE_ID);

        underTest.notifyAdmin(new PlacementApplicationEvent(CASE_DATA));

        verify(notificationService).sendEmail(
            PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE, CTSC_INBOX, NOTIFY_DATA, CASE_ID
        );
    }
}
