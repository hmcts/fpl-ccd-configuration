package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.fpl.events.NewJudicialMessageEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.JudicialMessageContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    NewJudicialMessageEventHandler.class,
    NotificationService.class,
    JudicialMessageContentProvider.class,
    ObjectMapper.class,
    CaseUrlService.class,
    FixedTimeConfiguration.class
})
class NewJudicialMessageEventHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final String APPLICATION_TYPE = "C19 - Warrant of assistance, 01 Janurary 2021, 12:00pm";
    private static final String RESPONDENT_LAST_NAME = "Watson";
    private static final Respondent RESPONDENT = Respondent.builder().party(RespondentParty.builder()
        .lastName(RESPONDENT_LAST_NAME).build())
        .build();

    @Autowired
    private NewJudicialMessageEventHandler underTest;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotifyJudicialMessageRecipientWhenANewMessageIsSentWithUrgency(boolean withUrgency) {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .respondents1(wrapElements(RESPONDENT))
            .build();

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .sender("paul@fpla.com")
            .recipient("david@fpla.com")
            .urgency(withUrgency ? "High" : null)
            .latestMessage("some query")
            .build();

        underTest.notifyJudicialMessageRecipient(new NewJudicialMessageEvent(caseData, judicialMessage));

        assertThat(response())
            .hasSubject("New message, " + RESPONDENT_LAST_NAME)
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
        CaseData caseData = CaseData.builder()
            .id(123L)
            .respondents1(wrapElements(RESPONDENT))
            .build();

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .sender("paul@fpla.com")
            .recipient("david@fpla.com")
            .urgency(null)
            .applicationType(withApplication ? APPLICATION_TYPE : null)
            .latestMessage("some query")
            .build();

        underTest.notifyJudicialMessageRecipient(new NewJudicialMessageEvent(caseData, judicialMessage));

        assertThat(response())
            .hasSubject("New message, " + RESPONDENT_LAST_NAME)
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
