package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons;
import uk.gov.hmcts.reform.fpl.events.AmendedReturnedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.content.ReturnedCaseContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    HmctsAdminNotificationHandler.class,
    ReturnedCaseContentProvider.class,
    CaseUrlService.class,
    AmendedReturnedCaseEventHandler.class,
    EmailNotificationHelper.class
})
class AmendedReturnedCaseEventHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final String FAMILY_MAN_CASE_NUMBER = "FAM_NUM";
    private static final long ID = 1234L;
    private static final String RESPONDENT_LAST_NAME = "Smith";
    private static final String CHILD_LAST_NAME = "Jones";
    private static final CaseData CASE_DATA = CaseData.builder()
        .id(ID)
        .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
        .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Will")
                .lastName(RESPONDENT_LAST_NAME)
                .build())
            .build()))
        .children1(wrapElements(Child.builder()
            .party(ChildParty.builder()
                .dateOfBirth(LocalDate.now())
                .lastName(CHILD_LAST_NAME)
                .build())
            .build()))
        .submittedForm(TestDataHelper.testDocumentReference())
        .returnApplication(ReturnApplication.builder()
            .reason(List.of(ReturnedApplicationReasons.INCOMPLETE))
            .note("please fill section 1")
            .build())
        .build();

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private AmendedReturnedCaseEventHandler underTest;

    @ParameterizedTest
    @MethodSource("subjectLineSource")
    void testAdminTemplate(boolean toggle, String name) {
        when(featureToggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);

        underTest.notifyAdmin(new AmendedReturnedCaseEvent(CASE_DATA));

        assertThat(response())
            .hasSubject("Amended application, " + name)
            .hasBody(emailContent()
                .line(LOCAL_AUTHORITY_NAME + " has amended its application for:")
                .line()
                .callout("Will Smith " + FAMILY_MAN_CASE_NUMBER)
                .line()
                .h1("Why the application was returned")
                .line("Application incomplete")
                .line()
                .line("please fill section 1")
                .line()
                .line("To view the application, sign in to:")
                .line()
                .line("http://fake-url/cases/case-details/1234")
                .line()
                .line("HM Courts & Tribunals Service")
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                     + "contactfpl@justice.gov.uk")
            );
    }

    @ParameterizedTest
    @MethodSource("subjectLineSource")
    void testCafcassTemplate(boolean toggle, String name) {
        when(featureToggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);

        underTest.notifyCafcass(new AmendedReturnedCaseEvent(CASE_DATA));

        assertThat(response())
            .hasSubject("Amended application, " + name)
            .hasBody(emailContent()
                .line(LOCAL_AUTHORITY_NAME + " has amended its application for:")
                .line()
                .callout("Will Smith " + FAMILY_MAN_CASE_NUMBER)
                .line()
                .h1("What we asked the local authority to change")
                .line("Application incomplete")
                .line()
                .line("please fill section 1")
                .line()
                .line("Download it at " + GOV_NOTIFY_DOC_URL)
                .line()
                .line("HM Courts & Tribunals Service")
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                     + "contactfpl@justice.gov.uk")
            );
    }

    private static Stream<Arguments> subjectLineSource() {
        return Stream.of(
            Arguments.of(true, CHILD_LAST_NAME),
            Arguments.of(false, RESPONDENT_LAST_NAME)
        );
    }
}
