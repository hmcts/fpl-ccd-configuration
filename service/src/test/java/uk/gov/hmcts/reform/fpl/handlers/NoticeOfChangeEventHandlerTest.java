package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.noticeofchange.NoticeOfChangeRespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfChangeContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_NEW_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(MockitoExtension.class)
class NoticeOfChangeEventHandlerTest {

    private static final String NEW_EMAIL = "new@test.com";
    private static final String OLD_EMAIL = "old@test.com";
    private static final RespondentSolicitor NEW_SOLICITOR = RespondentSolicitor.builder().email(NEW_EMAIL).build();
    private static final RespondentSolicitor OLD_SOLICITOR = RespondentSolicitor.builder().email(OLD_EMAIL).build();
    private static final CaseData CASE_DATA = caseData();
    private static final NoticeOfChangeRespondentSolicitorTemplate EXPECTED_TEMPLATE =
        NoticeOfChangeRespondentSolicitorTemplate.builder()
            .build();

    @Mock
    private NotificationService notificationService;

    @Mock
    private NoticeOfChangeContentProvider noticeOfChangeContentProvider;

    @InjectMocks
    private NoticeOfChangeEventHandler underTest;

    @Test
    void shouldSendEmailToSolicitorAccessGranted() {
        given(noticeOfChangeContentProvider.buildNoticeOfChangeRespondentSolicitorTemplate(CASE_DATA, NEW_SOLICITOR))
            .willReturn(EXPECTED_TEMPLATE);

        underTest.notifySolicitorAccessGranted(new NoticeOfChangeEvent(CASE_DATA, OLD_SOLICITOR, NEW_SOLICITOR));

        verify(notificationService).sendEmail(
            NOTICE_OF_CHANGE_NEW_REPRESENTATIVE,
            NEW_EMAIL,
            EXPECTED_TEMPLATE,
            CASE_DATA.getId());
    }

    @Test
    void shouldSendEmailToSolicitorAccessRevoked() {
        given(noticeOfChangeContentProvider.buildNoticeOfChangeRespondentSolicitorTemplate(CASE_DATA, OLD_SOLICITOR))
            .willReturn(EXPECTED_TEMPLATE);

        underTest.notifySolicitorAccessRevoked(new NoticeOfChangeEvent(CASE_DATA, OLD_SOLICITOR, NEW_SOLICITOR));

        verify(notificationService).sendEmail(
            NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE,
            OLD_EMAIL,
            EXPECTED_TEMPLATE,
            CASE_DATA.getId());
    }

    @Test
    void shouldNotSendEmailToSolicitorAccessRevokedWhenSolicitorIsNull() {
        underTest.notifySolicitorAccessRevoked(new NoticeOfChangeEvent(CASE_DATA, null, NEW_SOLICITOR));

        verify(notificationService, never()).sendEmail(
            NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE,
            OLD_EMAIL,
            EXPECTED_TEMPLATE,
            CASE_DATA.getId());
    }

    @Test
    void shouldNotSendEmailToSolicitorAccessRevokedWhenSolicitorEmailIsBlank() {
        RespondentSolicitor noEmailSolicitor = RespondentSolicitor.builder().email("").build();

        underTest.notifySolicitorAccessRevoked(new NoticeOfChangeEvent(CASE_DATA, noEmailSolicitor, NEW_SOLICITOR));

        verify(notificationService, never()).sendEmail(
            NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE,
            OLD_EMAIL,
            EXPECTED_TEMPLATE,
            CASE_DATA.getId());
    }
}
