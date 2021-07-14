package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.noticeofchange.NoticeOfChangeRespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfChangeContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_NEW_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(MockitoExtension.class)
class NoticeOfChangeEventHandlerTest {

    private static final String NEW_EMAIL = "new@test.com";
    private static final String OLD_EMAIL = "old@test.com";
    private static final Respondent NEW_RESPONDENT = buildNewRespondent();
    private static final Respondent OLD_RESPONDENT = buildOldRespondent();
    private static final CaseData CASE_DATA = caseData();
    private static final NoticeOfChangeRespondentSolicitorTemplate EXPECTED_TEMPLATE =
        NoticeOfChangeRespondentSolicitorTemplate.builder().build();
    public static final String RESPONDENT_FIRST_NAME = "John";
    public static final String RESPONDENT_LAST_NAME = "Smith";

    @Mock
    private NotificationService notificationService;

    @Mock
    private NoticeOfChangeContentProvider noticeOfChangeContentProvider;

    @InjectMocks
    private NoticeOfChangeEventHandler underTest;

    @Test
    void shouldSendEmailToSolicitorAccessGranted() {
        given(noticeOfChangeContentProvider.buildNoticeOfChangeRespondentSolicitorTemplate(CASE_DATA, NEW_RESPONDENT))
            .willReturn(EXPECTED_TEMPLATE);

        underTest.notifySolicitorAccessGranted(new NoticeOfChangeEvent(CASE_DATA, OLD_RESPONDENT, NEW_RESPONDENT));

        verify(notificationService).sendEmail(
            NOTICE_OF_CHANGE_NEW_REPRESENTATIVE,
            NEW_EMAIL,
            EXPECTED_TEMPLATE,
            CASE_DATA.getId());
    }

    @Test
    void shouldSendEmailToSolicitorAccessRevoked() {
        given(noticeOfChangeContentProvider.buildNoticeOfChangeRespondentSolicitorTemplate(CASE_DATA, OLD_RESPONDENT))
            .willReturn(EXPECTED_TEMPLATE);

        underTest.notifySolicitorAccessRevoked(new NoticeOfChangeEvent(CASE_DATA, OLD_RESPONDENT, NEW_RESPONDENT));

        verify(notificationService).sendEmail(
            NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE,
            OLD_EMAIL,
            EXPECTED_TEMPLATE,
            CASE_DATA.getId());
    }

    @Test
    void shouldNotSendEmailToSolicitorAccessRevokedWhenSolicitorIsNull() {
        underTest.notifySolicitorAccessRevoked(
            new NoticeOfChangeEvent(CASE_DATA, Respondent.builder().build(), NEW_RESPONDENT));

        verifyNoInteractions(noticeOfChangeContentProvider, notificationService);
    }

    @Test
    void shouldNotSendEmailToSolicitorAccessRevokedWhenSolicitorEmailIsBlank() {
        Respondent respondentWithNoEmailSolicitor = Respondent.builder()
            .party(RespondentParty.builder().firstName(RESPONDENT_FIRST_NAME).lastName(RESPONDENT_LAST_NAME).build())
            .solicitor(RespondentSolicitor.builder().email("").build())
            .build();

        underTest.notifySolicitorAccessRevoked(
            new NoticeOfChangeEvent(CASE_DATA, respondentWithNoEmailSolicitor, NEW_RESPONDENT));

        verifyNoInteractions(noticeOfChangeContentProvider, notificationService);
    }

    private static Respondent buildNewRespondent() {
        return Respondent.builder()
            .solicitor(RespondentSolicitor.builder().firstName("David").lastName("Jones").email(NEW_EMAIL).build())
            .party(RespondentParty.builder().firstName(RESPONDENT_FIRST_NAME).lastName(RESPONDENT_LAST_NAME).build())
            .build();
    }

    private static Respondent buildOldRespondent() {
        return Respondent.builder()
            .solicitor(RespondentSolicitor.builder().firstName("Jane").lastName("Taylor").email(OLD_EMAIL).build())
            .party(RespondentParty.builder().firstName(RESPONDENT_FIRST_NAME).lastName(RESPONDENT_LAST_NAME).build())
            .build();
    }

}
