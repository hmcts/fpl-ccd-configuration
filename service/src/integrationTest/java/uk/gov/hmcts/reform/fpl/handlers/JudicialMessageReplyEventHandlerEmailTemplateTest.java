package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.events.JudicialMessageReplyEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.content.JudicialMessageReplyContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    JudicialMessageReplyEventHandler.class,
    JudicialMessageReplyContentProvider.class,
    CaseUrlService.class,
    FixedTimeConfiguration.class,
    EmailNotificationHelper.class
})
class JudicialMessageReplyEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final String RESPONDENT_LAST_NAME = "Watson";
    private static final Respondent RESPONDENT = Respondent.builder()
        .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
        .build();
    private static final String CHILD_LAST_NAME = "Holmes";
    private static final Child CHILD = Child.builder()
        .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
        .build();

    @MockBean
    private FeatureToggleService toggleService;
    @Autowired
    private JudicialMessageReplyEventHandler underTest;

    @ParameterizedTest
    @MethodSource({"subjectLineSource"})
    void shouldNotifyJudicialMessageRecipientWhenAJudicialMessageIsReplied(boolean toggle, String name) {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .respondents1(wrapElements(RESPONDENT))
            .children1(wrapElements(CHILD))
            .build();

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .sender("david@fpla.com")
            .recipient("paul@fpla.com")
            .urgency("High")
            .latestMessage("some reply")
            .messageHistory("paul@fpla.com - some query")
            .build();

        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);

        underTest.notifyRecipientOfReply(new JudicialMessageReplyEvent(caseData, judicialMessage));

        assertThat(response())
            .hasSubject("New message, " + name)
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

    private static Stream<Arguments> subjectLineSource() {
        return Stream.of(
            Arguments.of(true, CHILD_LAST_NAME),
            Arguments.of(false, RESPONDENT_LAST_NAME)
        );
    }
}
