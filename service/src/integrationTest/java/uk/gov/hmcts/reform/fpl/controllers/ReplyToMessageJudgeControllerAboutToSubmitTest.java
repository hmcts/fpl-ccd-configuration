package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType;
import uk.gov.hmcts.reform.fpl.enums.OrganisationalRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessageMetaData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessageReply;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

@WebMvcTest(ReplyToMessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class ReplyToMessageJudgeControllerAboutToSubmitTest extends MessageJudgeControllerAbstractTest {
    private static final JudicialMessageRoleType SENDER_TYPE = JudicialMessageRoleType.LOCAL_COURT_ADMIN;
    private static final JudicialMessageRoleType RECIPIENT_TYPE = JudicialMessageRoleType.OTHER;
    private static final String SENDER = "ben@fpla.com";
    private static final String MESSAGE = "Some message";
    private static final String REPLY = "Some reply";
    private static final String MESSAGE_REQUESTED_BY = "request review from some court";
    private static final String MESSAGE_RECIPIENT = "recipient@fpla.com";
    private static final UUID SELECTED_DYNAMIC_LIST_ITEM_ID = UUID.randomUUID();
    private static final UUID ORIGINAL_MESSAGE_ID = UUID.randomUUID();

    ReplyToMessageJudgeControllerAboutToSubmitTest() {
        super("reply-message-judge");
    }

    @MockBean
    private UserService userService;

    @Test
    void shouldUpdateExistingJudicialMessageAndSortIntoExistingJudicialMessageListWhenReplying() {
        // We are an "OTHER" judge (original recipient) REPLYING to a "LOCAL COURT ADMIN" message (original sender)
        when(userService.getOrgRoles()).thenReturn(Set.of(OrganisationalRole.JUDGE));
        when(userService.getJudicialCaseRoles(any())).thenReturn(Set.of());

        String originalDateSent = formatLocalDateTimeBaseUsingFormat(now().minusDays(1), DATE_TIME_AT);

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageDynamicList(buildDynamicList(0,
                Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, originalDateSent)))
            .judicialMessageReply(JudicialMessage.builder()
                .isReplying(YesNo.YES.getValue())
                .latestMessage(REPLY)
                .replyFrom(MESSAGE_RECIPIENT)
                .recipientDynamicList(buildRecipientDynamicListNoJudges().toBuilder()
                    .value(DynamicListElement.builder()
                        .code(SENDER_TYPE.toString())
                        .build())
                    .build())
                .replyTo(SENDER)
                .senderType(RECIPIENT_TYPE)
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(List.of(
                element(SELECTED_DYNAMIC_LIST_ITEM_ID, buildJudicialMessage(originalDateSent, MESSAGE))))
            .build();

        when(userService.getUserEmail()).thenReturn(MESSAGE_RECIPIENT);

        CaseData responseCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        JudicialMessage expectedUpdatedJudicialMessage = JudicialMessage.builder()
            .sender(MESSAGE_RECIPIENT)
            .senderType(RECIPIENT_TYPE)
            .recipient(SENDER)
            .recipientType(SENDER_TYPE)
            .subject(MESSAGE_REQUESTED_BY)
            .updatedTime(now())
            .status(OPEN)
            .fromLabel("%s (%s)".formatted(RECIPIENT_TYPE.getLabel(), MESSAGE_RECIPIENT))
            .toLabel("%s (%s)".formatted(SENDER_TYPE.getLabel(), SENDER))
            .latestMessage(REPLY)
            .judicialMessageReplies(List.of(
                element(responseCaseData.getJudicialMessages().get(0).getValue().getJudicialMessageReplies()
                    .get(0).getId(),
                    JudicialMessageReply.builder()
                        .dateSent(formatLocalDateTimeBaseUsingFormat(now(), DATE_TIME_AT))
                        .updatedTime(now())
                        .message(REPLY)
                        .replyFrom("%s (%s)".formatted(RECIPIENT_TYPE.getLabel(), MESSAGE_RECIPIENT))
                        .replyTo("%s (%s)".formatted(SENDER_TYPE.getLabel(), SENDER))
                        .build()),
                element(ORIGINAL_MESSAGE_ID,
                    JudicialMessageReply.builder()
                        .dateSent(formatLocalDateTimeBaseUsingFormat(now().minusDays(2), DATE_TIME_AT))
                        .updatedTime(now().minusDays(2))
                        .message(MESSAGE)
                        .replyFrom("%s (%s)".formatted(SENDER_TYPE.getLabel(), SENDER))
                        .replyTo("%s (%s)".formatted(RECIPIENT_TYPE.getLabel(), MESSAGE_RECIPIENT))
                        .build())
                ))
            .dateSent(formatLocalDateTimeBaseUsingFormat(now(), DATE_TIME_AT))
            .build();

        assertThat(responseCaseData.getJudicialMessages())
            .first()
            .isEqualTo(element(SELECTED_DYNAMIC_LIST_ITEM_ID, expectedUpdatedJudicialMessage));
        assertThat(responseCaseData.getLatestRoleSent()).isEqualTo(SENDER_TYPE);
    }

    @Test
    void shouldCloseJudicialMessageAndSortTheClosedJudicialMessagesListWhenClosingAMessage() {
        String dateSent = formatLocalDateTimeBaseUsingFormat(now().minusDays(1), DATE_TIME_AT);

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageDynamicList(buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, dateSent)))
            .judicialMessageReply(JudicialMessage.builder()
                .isReplying(YesNo.NO.getValue())
                .latestMessage(null)
                .build())
            .build();

        Element<JudicialMessage> selectedOpenMessage = element(
            SELECTED_DYNAMIC_LIST_ITEM_ID, buildJudicialMessage(dateSent, MESSAGE));

        Element<JudicialMessage> oldOpenMessage = element(UUID.randomUUID(), buildJudicialMessage(
            formatLocalDateTimeBaseUsingFormat(now().minusDays(2), DATE_TIME_AT), null));

        Element<JudicialMessage> closedMessage = element(UUID.randomUUID(), buildJudicialMessage(
            formatLocalDateTimeBaseUsingFormat(now().minusDays(2), DATE_TIME_AT), null));

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(List.of(selectedOpenMessage, oldOpenMessage))
            .closedJudicialMessages(List.of(closedMessage))
            .build();

        JudicialMessage expectedClosedJudicialMessage =
            selectedOpenMessage.getValue().toBuilder().status(CLOSED).updatedTime(now()).build();

        when(userService.getUserEmail()).thenReturn(MESSAGE_RECIPIENT);

        CaseData responseCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseCaseData.getClosedJudicialMessages())
            .containsExactly(element(SELECTED_DYNAMIC_LIST_ITEM_ID, expectedClosedJudicialMessage), closedMessage);

        assertThat(responseCaseData.getJudicialMessages()).containsOnly(oldOpenMessage);
    }

    @Test
    void shouldRemoveTransientFields() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.ofEntries(
                Map.entry("hasAdditionalApplications", "some data"),
                Map.entry("isMessageRegardingDocuments", "APPLICATION"),
                Map.entry("additionalApplicationsDynamicList",
                    buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, "some data"))),
                Map.entry("relatedDocumentsLabel", "some data"),
                Map.entry("replyToMessageJudgeNextHearingLabel", "some data"),
                Map.entry("judicialMessageMetaData", JudicialMessageMetaData.builder()
                    .recipient("some data")
                    .sender("some data")
                    .urgency("some data")
                    .build()),
                Map.entry("judicialMessageNote", "some data"),
                Map.entry("judicialMessageDynamicList",
                    buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, "some data"))),
                Map.entry("judicialMessageReply", JudicialMessage.builder()
                    .recipientDynamicList(buildRecipientDynamicListNoJudges().toBuilder()
                        .value(DynamicListElement.builder()
                            .code(RECIPIENT_TYPE.toString())
                            .build())
                        .build())
                    .recipientType(JudicialMessageRoleType.CTSC).build())
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getData()).doesNotContainKeys(
            "hasAdditionalApplications",
            "isMessageRegardingDocuments",
            "additionalApplicationsDynamicList",
            "relatedDocumentsLabel",
            "nextHearingLabel",
            "judicialMessageMetaData",
            "judicialMessageNote",
            "judicialMessageDynamicList",
            "judicialMessageReply"
        );
        assertThat(response.getData()).containsKey("latestRoleSent");
    }

    private JudicialMessage buildJudicialMessage(String dateSent, String latestMessage) {
        return JudicialMessage.builder()
            .sender(SENDER)
            .recipient(MESSAGE_RECIPIENT)
            .updatedTime(now().minusDays(2))
            .status(OPEN)
            .subject(MESSAGE_REQUESTED_BY)
            .latestMessage(latestMessage)
            .judicialMessageReplies(List.of(element(ORIGINAL_MESSAGE_ID,
                JudicialMessageReply.builder()
                    .dateSent(formatLocalDateTimeBaseUsingFormat(now().minusDays(2), DATE_TIME_AT))
                    .updatedTime(now().minusDays(2))
                    .message(MESSAGE)
                    .replyFrom("%s (%s)".formatted(SENDER_TYPE.getLabel(), SENDER))
                    .replyTo("%s (%s)".formatted(RECIPIENT_TYPE.getLabel(), MESSAGE_RECIPIENT))
                    .build())))
            .dateSent(dateSent)
            .build();
    }
}
