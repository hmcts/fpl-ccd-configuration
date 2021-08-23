package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDICIAL_MESSAGE_ADDED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDICIAL_MESSAGE_REPLY_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_NAME;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(MessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class MessageJudgeControllerSubmittedTest extends AbstractCallbackTest {
    private static final String JUDICIAL_MESSAGE_RECIPIENT = "recipient@test.com";
    private static final Long CASE_ID = 12345L;
    private static final UUID SELECTED_DYNAMIC_LIST_ITEM_ID = UUID.randomUUID();
    private static final String MESSAGE = "Some note";
    private static final String REPLY = "Reply";
    private static final String LAST_NAME = "Davidson";

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
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
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .respondents1(List.of(
                element(Respondent.builder()
                    .party(RespondentParty.builder()
                        .lastName("Davidson")
                        .build())
                    .build())))
            .children1(List.of(
                element(Child.builder()
                    .party(ChildParty.builder()
                        .lastName(LAST_NAME)
                        .dateOfBirth(dateNow())
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
            "caseUrl", caseUrl(CASE_ID, "Judicial messages"),
            "callout", "^Davidson",
            "sender", "sender@fpla.com",
            "urgency", "High",
            "hasUrgency", "Yes",
            "hasApplication", "No",
            "applicationType", "",
            "latestMessage", MESSAGE
        );

        verify(notificationClient).sendEmail(
            JUDICIAL_MESSAGE_ADDED_TEMPLATE, JUDICIAL_MESSAGE_RECIPIENT, expectedData, notificationReference(CASE_ID));
        verify(coreCaseDataService).triggerEvent(JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            "internal-update-case-summary",
            caseSummary());
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
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .respondents1(List.of(
                element(Respondent.builder()
                    .party(RespondentParty.builder()
                        .lastName(LAST_NAME)
                        .build())
                    .build())))
            .children1(List.of(
                element(Child.builder()
                    .party(ChildParty.builder()
                        .lastName(LAST_NAME)
                        .dateOfBirth(dateNow())
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
            "caseUrl", caseUrl(CASE_ID, "Judicial messages"),
            "callout", "^Davidson",
            "hasApplication", "No",
            "applicationType", "",
            "latestMessage", REPLY
        );

        verify(notificationClient).sendEmail(
            JUDICIAL_MESSAGE_REPLY_TEMPLATE, JUDICIAL_MESSAGE_RECIPIENT, expectedData, notificationReference(CASE_ID));
        verify(coreCaseDataService).triggerEvent(JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            "internal-update-case-summary",
            caseSummary());
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
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .respondents1(List.of(
                element(Respondent.builder()
                    .party(RespondentParty.builder()
                        .lastName(LAST_NAME)
                        .build())
                    .build())))
            .children1(List.of(
                element(Child.builder()
                    .party(ChildParty.builder()
                        .lastName(LAST_NAME)
                        .dateOfBirth(dateNow())
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
        verify(coreCaseDataService).triggerEvent(JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            "internal-update-case-summary",
            caseSummary());
    }

    private Map<String, Object> caseSummary() {
        return caseConverter.toMap(
            SyntheticCaseSummary.builder()
                .caseSummaryHasUnresolvedMessages("Yes")
                .caseSummaryFirstRespondentLastName(LAST_NAME)
                .caseSummaryCourtName(COURT_NAME)
                .caseSummaryNumberOfChildren(1)
                .build());
    }
}
