package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.noticeofchange.NoticeOfChangeRespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfChangeContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_NEW_REPRESENTATIVE;

@ExtendWith(MockitoExtension.class)
class NoticeOfChangeEventHandlerTest {

    private static final Long CASE_ID = 1L;
    private static final String NEW_EMAIL = "new@test.com";
    private static final String OLD_EMAIL = "old@test.com";
    private static final NoticeOfChangeRespondentSolicitorTemplate EXPECTED_TEMPLATE =
        mock(NoticeOfChangeRespondentSolicitorTemplate.class);

    private final CaseData caseData = mock(CaseData.class);
    private final WithSolicitor newParty = mock(WithSolicitor.class);
    private final WithSolicitor oldParty = mock(WithSolicitor.class);
    private final RespondentSolicitor solicitor = mock(RespondentSolicitor.class);

    @Mock
    private NotificationService notificationService;

    @Mock
    private NoticeOfChangeContentProvider noticeOfChangeContentProvider;

    @InjectMocks
    private NoticeOfChangeEventHandler underTest;

    @Test
    void shouldSendEmailToSolicitorAccessGranted() {
        given(caseData.getId()).willReturn(CASE_ID);
        given(newParty.getSolicitor()).willReturn(solicitor);
        given(solicitor.getEmail()).willReturn(NEW_EMAIL);

        given(noticeOfChangeContentProvider.buildNoticeOfChangeRespondentSolicitorTemplate(caseData, newParty))
            .willReturn(EXPECTED_TEMPLATE);

        underTest.notifySolicitorAccessGranted(new NoticeOfChangeEvent(caseData, oldParty, newParty));

        verify(notificationService).sendEmail(
            NOTICE_OF_CHANGE_NEW_REPRESENTATIVE,
            NEW_EMAIL,
            EXPECTED_TEMPLATE,
            CASE_ID
        );
    }

    @Test
    void shouldSendEmailToSolicitorAccessRevoked() {
        given(caseData.getId()).willReturn(CASE_ID);
        given(oldParty.getSolicitor()).willReturn(solicitor);
        given(solicitor.getEmail()).willReturn(OLD_EMAIL);

        given(noticeOfChangeContentProvider.buildNoticeOfChangeRespondentSolicitorTemplate(caseData, oldParty))
            .willReturn(EXPECTED_TEMPLATE);

        underTest.notifySolicitorAccessRevoked(new NoticeOfChangeEvent(caseData, oldParty, newParty));

        verify(notificationService).sendEmail(
            NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE,
            OLD_EMAIL,
            EXPECTED_TEMPLATE,
            CASE_ID
        );
    }

    @Test
    void shouldNotSendEmailToSolicitorAccessRevokedWhenSolicitorIsNull() {
        underTest.notifySolicitorAccessRevoked(new NoticeOfChangeEvent(caseData, oldParty, newParty));

        verifyNoInteractions(noticeOfChangeContentProvider, notificationService);
    }

    @Test
    void shouldNotSendEmailToSolicitorAccessRevokedWhenSolicitorEmailIsBlank() {
        given(oldParty.getSolicitor()).willReturn(solicitor);
        given(solicitor.getEmail()).willReturn("");

        underTest.notifySolicitorAccessRevoked(new NoticeOfChangeEvent(caseData, oldParty, newParty));

        verifyNoInteractions(noticeOfChangeContentProvider, notificationService);
    }
}
