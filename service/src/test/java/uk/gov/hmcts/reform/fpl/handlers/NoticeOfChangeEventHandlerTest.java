package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.noticeofchange.RespondentSolicitorNoticeOfChangeTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfChangeContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_NEW_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {NoticeOfChangeEventHandler.class})
public class NoticeOfChangeEventHandlerTest {

    private static final String expectedEmail = "test@test.com";
    private static final RespondentSolicitor solicitor = RespondentSolicitor.builder().email(expectedEmail).build();
    private static final  CaseData caseData = caseData();
    private static final RespondentSolicitorNoticeOfChangeTemplate expectedTemplate =
        RespondentSolicitorNoticeOfChangeTemplate.builder()
            .build();

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NoticeOfChangeContentProvider noticeOfChangeContentProvider;

    @Autowired
    private NoticeOfChangeEventHandler underTest;

    @Test
    void shouldSendEmailToSolicitorAccessGranted() {
        given(noticeOfChangeContentProvider.buildRespondentSolicitorAccessGrantedNotification(caseData, solicitor))
            .willReturn(expectedTemplate);

        underTest.notifySolicitorAccessGranted(new NoticeOfChangeEvent(caseData, solicitor, solicitor));

        verify(notificationService).sendEmail(
            NOTICE_OF_CHANGE_NEW_REPRESENTATIVE,
            expectedEmail,
            expectedTemplate,
            caseData.getId());
    }

    @Test
    void shouldSendEmailToSolicitorAccessRevoked() {
        given(noticeOfChangeContentProvider.buildRespondentSolicitorAccessRevokedNotification(caseData, solicitor))
            .willReturn(expectedTemplate);

        underTest.notifySolicitorAccessRevoked(new NoticeOfChangeEvent(caseData, solicitor, solicitor));

        verify(notificationService).sendEmail(
            NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE,
            expectedEmail,
            expectedTemplate,
            caseData.getId());
    }
}
