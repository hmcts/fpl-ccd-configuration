package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.events.cmo.CMORemovedEvent;
import uk.gov.hmcts.reform.fpl.handlers.cmo.CMORemovedEventHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderRemovalEmailContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    CMORemovedEventHandler.class,
    OrderRemovalEmailContentProvider.class,
    EmailNotificationHelper.class,
    CaseUrlService.class
})
class CMORemovedEventHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final String REMOVAL_REASON = "reason";
    private static final Long CASE_ID = 12345L;
    private static final String CHILD_LAST_NAME = "Holmes";
    private static final String RESPONDENT_LAST_NAME = "Watson";
    private static final CaseData CASE_DATA = CaseData.builder()
        .id(CASE_ID)
        .children1(wrapElements(Child.builder()
            .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
            .build()))
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
            .build()))
        .build();

    @MockBean
    private FeatureToggleService toggleService;

    @Autowired
    private CMORemovedEventHandler underTest;

    @ParameterizedTest
    @MethodSource("subjectLineSource")
    void notifyLA(boolean toggle, String name) {
        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);
        underTest.notifyLocalAuthorityOfRemovedCMO(new CMORemovedEvent(CASE_DATA, REMOVAL_REASON));

        assertThat(response())
            .hasSubject("Case management order removed, " + name)
            .hasBody(emailContent()
                .line("We've removed the case management order for the case:")
                .line()
                .callout(String.valueOf(CASE_ID))
                .line()
                .h1("Why the order was removed")
                .line()
                .line(REMOVAL_REASON)
                .line()
                .line("To view the case, sign in to:")
                .line()
                .line("http://fake-url/cases/case-details/" + CASE_ID)
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
