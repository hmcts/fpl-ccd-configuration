package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.fpl.events.JudicialMessageReplyEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.JudicialMessageReplyContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    JudicialMessageReplyEventHandler.class,
    NotificationService.class,
    JudicialMessageReplyContentProvider.class,
    ObjectMapper.class,
    CaseUrlService.class,
    FixedTimeConfiguration.class
})
class JudicialMessageReplyEventHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final String RESPONDENT_LAST_NAME = "Watson";
    private static final Respondent RESPONDENT = Respondent.builder().party(RespondentParty.builder()
        .lastName(RESPONDENT_LAST_NAME).build())
        .build();

    @Autowired
    private JudicialMessageReplyEventHandler underTest;

    @Test
    void shouldNotifyJudicialMessageRecipientWhenAJudicialMessageIsReplied() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .respondents1(wrapElements(RESPONDENT))
            .build();

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .sender("david@fpla.com")
            .recipient("paul@fpla.com")
            .urgency("High")
            .latestMessage("some reply")
            .messageHistory("paul@fpla.com - some query")
            .build();

        underTest.notifyRecipientOfReply(new JudicialMessageReplyEvent(caseData, judicialMessage));

        assertThat(response())
            .hasSubject("New message, " + RESPONDENT_LAST_NAME)
            .hasBody(emailContent()
                .start()
                .line("You've received a message about:")
                .line()
                .callout(RESPONDENT_LAST_NAME)
                .lines(3)
                .line("Message: some reply")
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
