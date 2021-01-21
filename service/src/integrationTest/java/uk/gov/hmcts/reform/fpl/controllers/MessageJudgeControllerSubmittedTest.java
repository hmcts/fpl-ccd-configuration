package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDICIAL_MESSAGE_ADDED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDICIAL_MESSAGE_REPLY_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(MessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class MessageJudgeControllerSubmittedTest extends AbstractControllerTest {
    private static final String JUDICIAL_MESSAGE_RECIPIENT = "recipient@test.com";
    private static final Long CASE_REFERENCE = 12345L;
    private static final UUID SELECTED_DYNAMIC_LIST_ITEM_ID = UUID.randomUUID();
    private static final String MESSAGE = "Some note";
    private static final String REPLY = "Reply";

    @MockBean
    private NotificationClient notificationClient;

    @MockBean // TODO: substitute with actual call
    private CoreCaseDataService coreCaseDataService;

    MessageJudgeControllerSubmittedTest() {
        super("message-judge");
    }

    @Test
    void shouldNotifyJudicialMessageRecipientWhenNewJudicialMessageAdded() throws NotificationClientException {
        JudicialMessage latestJudicialMessage = JudicialMessage.builder()
            .updatedTime(now())
            .status(OPEN)
            .recipient(JUDICIAL_MESSAGE_RECIPIENT)
            .sender("sender@fpla.com")
            .urgency("High")
            .messageHistory(String.format("%s - %s", "sender@fpla.com", MESSAGE))
            .latestMessage(MESSAGE)
            .build();

        CaseData caseData = CaseData.builder()
            .id(CASE_REFERENCE)
            .respondents1(List.of(
                element(Respondent.builder()
                    .party(RespondentParty.builder()
                        .lastName("Davidson")
                        .build())
                    .build())))
            .judicialMessages(List.of(
                element(latestJudicialMessage),
                element(JudicialMessage.builder()
                    .updatedTime(now().minusDays(1))
                    .status(OPEN)
                    .recipient("do_not_send@fpla.com")
                    .sender("someOthersender@fpla.com")
                    .urgency("High")
                    .build())))
            .build();

        postSubmittedEvent(asCaseDetails(caseData));

        Map<String, Object> expectedData = Map.of(
            "respondentLastName", "Davidson",
            "caseUrl", "http://fake-url/cases/case-details/12345#Judicial%20messages",
            "callout", "^Davidson",
            "sender", "sender@fpla.com",
            "urgency", "High",
            "hasUrgency", "Yes",
            "latestMessage", MESSAGE
        );

        verify(notificationClient).sendEmail(
            JUDICIAL_MESSAGE_ADDED_TEMPLATE, JUDICIAL_MESSAGE_RECIPIENT, expectedData, "localhost/12345");
    }

    @Test
    void shouldNotifyJudicialMessageRecipientWhenJudicialMessageReplyAdded() throws NotificationClientException {
        JudicialMessage latestJudicialMessage = JudicialMessage.builder()
            .recipient(JUDICIAL_MESSAGE_RECIPIENT)
            .updatedTime(now().minusDays(1))
            .status(OPEN)
            .sender("sender@fpla.com")
            .urgency("High")
            .latestMessage(REPLY)
            .messageHistory(MESSAGE + "/n" + REPLY)
            .build();

        CaseData caseData = CaseData.builder()
            .id(CASE_REFERENCE)
            .respondents1(List.of(
                element(Respondent.builder()
                    .party(RespondentParty.builder()
                        .lastName("Davidson")
                        .build())
                    .build())))
            .judicialMessages(List.of(
                element(SELECTED_DYNAMIC_LIST_ITEM_ID, latestJudicialMessage),
                element(JudicialMessage.builder()
                    .updatedTime(now().minusDays(3))
                    .status(OPEN)
                    .recipient("do_not_send@fpla.com")
                    .sender("someOthersender@fpla.com")
                    .urgency("High")
                    .build())))
            .build();

        postSubmittedEvent(asCaseDetails(caseData));

        Map<String, Object> expectedData = Map.of(
            "respondentLastName", "Davidson",
            "caseUrl", "http://fake-url/cases/case-details/12345#Judicial%20messages",
            "callout", "^Davidson",
            "latestMessage", REPLY
        );

        verify(notificationClient).sendEmail(
            JUDICIAL_MESSAGE_REPLY_TEMPLATE, JUDICIAL_MESSAGE_RECIPIENT, expectedData, "localhost/12345");
    }

    @Test
    void shouldNotSendEmailNotificationsWhenJudicialMessageIsClosed() {
        JudicialMessage latestJudicialMessage = JudicialMessage.builder()
            .recipient(JUDICIAL_MESSAGE_RECIPIENT)
            .updatedTime(now())
            .status(CLOSED)
            .sender("sender@fpla.com")
            .urgency("High")
            .latestMessage(null)
            .messageHistory(MESSAGE + "/n" + REPLY)
            .build();

        CaseData caseData = CaseData.builder()
            .id(CASE_REFERENCE)
            .respondents1(List.of(
                element(Respondent.builder()
                    .party(RespondentParty.builder()
                        .lastName("Davidson")
                        .build())
                    .build())))
            .judicialMessages(List.of(
                element(JudicialMessage.builder()
                    .updatedTime(now().minusDays(1))
                    .status(OPEN)
                    .recipient("do_not_send@fpla.com")
                    .sender("someOthersender@fpla.com")
                    .urgency("High")
                    .build())))
            .closedJudicialMessages(List.of(element(SELECTED_DYNAMIC_LIST_ITEM_ID, latestJudicialMessage)))
            .build();

        postSubmittedEvent(asCaseDetails(caseData));

        verifyNoInteractions(notificationClient);
    }
}
