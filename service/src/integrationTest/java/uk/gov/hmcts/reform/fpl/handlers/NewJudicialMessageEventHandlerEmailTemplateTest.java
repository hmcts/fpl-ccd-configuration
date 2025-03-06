package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.NewJudicialMessageEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.content.JudicialMessageContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    NewJudicialMessageEventHandler.class,
    JudicialMessageContentProvider.class,
    CaseUrlService.class,
    FixedTimeConfiguration.class,
    EmailNotificationHelper.class
})
class NewJudicialMessageEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final String APPLICATION_TYPE = "C19 - Warrant of assistance, 01 January 2021, 12:00pm";
    private static final String CHILD_LAST_NAME = "Holmes";
    private static final Child CHILD = Child.builder()
        .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
        .build();
    private static final String RESPONDENT_LAST_NAME = "Watson";
    private static final Respondent RESPONDENT = Respondent.builder()
        .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
        .build();
    private static final CaseData CASE_DATA = CaseData.builder()
        .id(123L)
        .respondents1(wrapElements(RESPONDENT))
        .children1(wrapElements(CHILD))
        .build();

    @MockBean
    private CtscEmailLookupConfiguration ctscEmailLookupConfiguration;

    @Autowired
    private NewJudicialMessageEventHandler underTest;

    @BeforeEach
    void setup() {
        given(ctscEmailLookupConfiguration.getEmail()).willReturn("ctsc@test.com");
        given(featureToggleService.isCourtNotificationEnabledForWa(any())).willReturn(true);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotifyJudicialMessageRecipientWhenANewMessageIsSentWithUrgency(boolean withUrgency) {
        JudicialMessage judicialMessage = JudicialMessage.builder()
            .sender("paul@fpla.com")
            .recipient("david@fpla.com")
            .urgency(withUrgency ? "High" : null)
            .latestMessage("some query")
            .build();

        underTest.notifyJudicialMessageRecipient(new NewJudicialMessageEvent(CASE_DATA, judicialMessage));

        assertThat(response())
            .hasSubject("New message, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .start()
                .line("You've received a message about:")
                .line()
                .callout(RESPONDENT_LAST_NAME)
                .line()
                .line()
                .line()
                .line("Enquiry from: paul@fpla.com")
                .line(withUrgency ? "Response requested: High" : "")
                .line("Message: some query")
                .line()
                .line("To respond, sign in to:")
                .line()
                .line("http://fake-url/cases/case-details/123#Judicial%20messages")
                .line()
                .line("HM Courts & Tribunals Service")
                .end("Do not reply to this email. If you need to contact us, "
                    + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotifyJudicialMessageRecipientWhenANewMessageIsSentWithApplication(boolean withApplication) {
        JudicialMessage judicialMessage = JudicialMessage.builder()
            .sender("paul@fpla.com")
            .recipient("david@fpla.com")
            .urgency(null)
            .applicationType(withApplication ? APPLICATION_TYPE : null)
            .latestMessage("some query")
            .build();

        underTest.notifyJudicialMessageRecipient(new NewJudicialMessageEvent(CASE_DATA, judicialMessage));

        assertThat(response())
            .hasSubject("New message, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .start()
                .line("You've received a message about:")
                .line()
                .callout(RESPONDENT_LAST_NAME)
                .line()
                .line(withApplication ? "Regarding: " + APPLICATION_TYPE : "")
                .line()
                .line("Enquiry from: paul@fpla.com")
                .line()
                .line("Message: some query")
                .line()
                .line("To respond, sign in to:")
                .line()
                .line("http://fake-url/cases/case-details/123#Judicial%20messages")
                .line()
                .line("HM Courts & Tribunals Service")
                .end("Do not reply to this email. If you need to contact us, "
                    + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }
}
