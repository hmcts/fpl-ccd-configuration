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
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ccd.CCDConcurrencyHelper;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDICIAL_MESSAGE_REPLY_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(ReplyToMessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class ReplyToMessageJudgeControllerSubmittedTest extends AbstractCallbackTest {
    private static final String JUDICIAL_MESSAGE_RECIPIENT = "recipient@test.com";
    private static final Long CASE_ID = 12345L;
    private static final UUID SELECTED_DYNAMIC_LIST_ITEM_ID = UUID.randomUUID();
    private static final String MESSAGE = "Some note";
    private static final String REPLY = "Reply";
    private static final String LAST_NAME = "Davidson";

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CCDConcurrencyHelper concurrencyHelper;

    @MockBean
    private FeatureToggleService featureToggleService;

    ReplyToMessageJudgeControllerSubmittedTest() {
        super("reply-message-judge");
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

        when(featureToggleService.isCourtNotificationEnabledForWa(any())).thenReturn(true);

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
        verifyNoInteractions(concurrencyHelper);
    }

    @Test
    void shouldNotNotifyJudicialMessageRecipientIfToggledOff() throws NotificationClientException {
        when(featureToggleService.isWATaskEmailsEnabled()).thenReturn(false);

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

        verify(notificationClient, never()).sendEmail(
            JUDICIAL_MESSAGE_REPLY_TEMPLATE, JUDICIAL_MESSAGE_RECIPIENT, expectedData, notificationReference(CASE_ID));
        verifyNoInteractions(concurrencyHelper);
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
        verifyNoInteractions(concurrencyHelper);
    }

}
