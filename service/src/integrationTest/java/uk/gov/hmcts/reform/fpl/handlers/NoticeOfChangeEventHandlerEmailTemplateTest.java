package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.fpl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfChangeContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;

import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;

@SpringBootTest(classes = {
    NoticeOfChangeEventHandler.class,
    NotificationService.class,
    NoticeOfChangeContentProvider.class,
    ObjectMapper.class,
    CaseUrlService.class,
})
class NoticeOfChangeEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final Long CASE_ID = 12345L;
    private static final String CASE_NAME = "Test";
    private static final String CASE_URL = "http://fake-url/cases/case-details/" + CASE_ID;
    private static final String SOLICITOR_FIRST_NAME = "John";
    private static final String SOLICITOR_LAST_NAME = "Watson";
    private static final String EXPECTED_SALUTATION = "Dear John Watson";

    private static final CaseData CASE_DATA = CaseData.builder()
        .id(CASE_ID)
        .caseName(CASE_NAME)
        .build();

    @Autowired
    private NoticeOfChangeEventHandler underTest;

    @Test
    void notifySolicitorAccessGranted() {

        RespondentSolicitor oldSolicitor = RespondentSolicitor.builder().build();

        RespondentSolicitor newSolicitor = RespondentSolicitor.builder()
            .firstName(SOLICITOR_FIRST_NAME)
            .lastName(SOLICITOR_LAST_NAME)
            .email("test@test.com")
            .build();

        underTest.notifySolicitorAccessGranted(new NoticeOfChangeEvent(CASE_DATA, oldSolicitor, newSolicitor));

        assertThat(response())
            .hasSubject("Notice of change completed")
            .hasBody(emailContent()
                .line(EXPECTED_SALUTATION)
                .line()
                .line("You’ve completed the Notice of acting or Notice of change in the case:")
                .line()
                .line(CASE_NAME + " " + CASE_ID)
                .line()
                .line("You can now view case details by signing into " + CASE_URL)
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end(
                    "Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                        + "contactfpl@justice.gov.uk")
            );
    }

    @Test
    void notifySolicitorAccessRevoked() {

        RespondentSolicitor oldSolicitor = RespondentSolicitor.builder()
            .firstName(SOLICITOR_FIRST_NAME)
            .lastName(SOLICITOR_LAST_NAME)
            .email("test@test.com")
            .build();

        RespondentSolicitor newSolicitor = RespondentSolicitor.builder().build();

        underTest.notifySolicitorAccessRevoked(new NoticeOfChangeEvent(CASE_DATA, oldSolicitor, newSolicitor));

        assertThat(response())
            .hasSubject("FPL case access revoked")
            .hasBody(emailContent()
                .line(EXPECTED_SALUTATION)
                .line()
                .line("A new notice of change has been completed for the case:")
                .line()
                .line(CASE_NAME + " " + CASE_ID)
                .line()
                .line(
                    "The respondent’s new legal representative will now have online access to the case. Your "
                        + "organisation’s access has been revoked.")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end(
                    "Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                        + "contactfpl@justice.gov.uk")
            );
    }
}
