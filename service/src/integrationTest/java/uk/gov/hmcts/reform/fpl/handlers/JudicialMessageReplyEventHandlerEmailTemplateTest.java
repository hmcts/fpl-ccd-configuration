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

import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@SpringBootTest(classes = {
    JudicialMessageReplyEventHandler.class,
    NotificationService.class,
    JudicialMessageReplyContentProvider.class,
    ObjectMapper.class,
    CaseUrlService.class
})
public class JudicialMessageReplyEventHandlerEmailTemplateTest extends EmailTemplateTest {

    public static final Respondent RESPONDENT = Respondent.builder().party(RespondentParty.builder()
        .lastName("Watson").build())
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
            .hasSubject("New message, " + getFirstRespondentLastName(caseData))
            .hasBody(emailContent()
                .start()
                .line("You've received a message about:")
                .line()
                .line(buildCallout(caseData))
                .line()
                .line("Message: some reply")
                .line()
                .line("To respond, sign in to:")
                .line()
                .line("http://fake-url/cases/case-details/123#JudicialMessagesTab")
                .line()
                .line("HM Courts & Tribunals Service")
                .end("Do not reply to this email. If you need to contact us, "
                    + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }
}
