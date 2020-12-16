package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.MessageJudgeOptions;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.JudicialMessageMetaData;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

@ActiveProfiles("integration-test")
@WebMvcTest(MessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class MessageJudgeControllerAboutToSubmitTest extends AbstractControllerTest {
    private static final String SENDER = "ben@fpla.com";
    private static final String MESSAGE = "Some message";
    private static final String REPLY = "Some reply";
    private static final String MESSAGE_REQUESTED_BY = "request review from some court";
    private static final String MESSAGE_RECIPIENT = "recipient@fpla.com";
    private static final UUID SELECTED_DYNAMIC_LIST_ITEM_ID = UUID.randomUUID();

    MessageJudgeControllerAboutToSubmitTest() {
        super("message-judge");
    }

    @MockBean
    private IdentityService identityService;

    @MockBean
    private UserService userService;

    @Test
    void shouldAddNewJudicialMessageAndSortIntoExistingJudicialMessageList() {
        JudicialMessage oldJudicialMessage = JudicialMessage.builder()
            .updatedTime(now().minusDays(1))
            .build();

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .relatedDocumentsLabel("related documents")
            .judicialMessageNote(MESSAGE)
            .judicialMessageMetaData(JudicialMessageMetaData.builder()
                .urgency("High urgency")
                .recipient(MESSAGE_RECIPIENT)
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(1111L)
            .judicialMessages(List.of(element(oldJudicialMessage)))
            .messageJudgeEventData(messageJudgeEventData)
            .build();

        when(identityService.generateId()).thenReturn(SELECTED_DYNAMIC_LIST_ITEM_ID);
        when(userService.getUserEmail()).thenReturn(SENDER);

        CaseData responseCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        JudicialMessage expectedJudicialMessage = JudicialMessage.builder()
            .dateSent(formatLocalDateTimeBaseUsingFormat(now(), DATE_TIME_AT))
            .updatedTime(now())
            .status(OPEN)
            .recipient(MESSAGE_RECIPIENT)
            .latestMessage(MESSAGE)
            .sender(SENDER)
            .messageHistory(MESSAGE)
            .urgency("High urgency")
            .build();

        assertThat(responseCaseData.getJudicialMessages().get(0).getValue()).isEqualTo(expectedJudicialMessage);
        assertThat(responseCaseData.getJudicialMessages().get(1).getValue()).isEqualTo(oldJudicialMessage);
    }

    @Test
    void shouldUpdateExistingJudicialMessageAndSortIntoExistingJudicialMessageListWhenReplying() {
        String dateSent = formatLocalDateTimeBaseUsingFormat(now().minusDays(1), DATE_TIME_AT);

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageDynamicList(buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, dateSent)))
            .judicialMessageReply(JudicialMessage.builder()
                .latestMessage(REPLY)
                .build())
            .messageJudgeOption(MessageJudgeOptions.REPLY)
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(List.of(
                element(SELECTED_DYNAMIC_LIST_ITEM_ID, JudicialMessage.builder()
                    .sender(SENDER)
                    .recipient(MESSAGE_RECIPIENT)
                    .updatedTime(now().minusDays(1))
                    .status(OPEN)
                    .requestedBy(MESSAGE_REQUESTED_BY)
                    .latestMessage(MESSAGE)
                    .messageHistory(MESSAGE)
                    .dateSent(dateSent)
                    .build())))
            .build();

        JudicialMessage expectedUpdatedJudicialMessage = JudicialMessage.builder()
            .sender(MESSAGE_RECIPIENT)
            .recipient(SENDER)
            .requestedBy(MESSAGE_REQUESTED_BY)
            .updatedTime(now())
            .status(OPEN)
            .latestMessage(REPLY)
            .messageHistory(MESSAGE + "\n" + REPLY)
            .dateSent(dateSent)
            .build();

        when(userService.getUserEmail()).thenReturn(MESSAGE_RECIPIENT);

        CaseData responseCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseCaseData.getJudicialMessages())
            .first()
            .isEqualTo(element(SELECTED_DYNAMIC_LIST_ITEM_ID, expectedUpdatedJudicialMessage));
    }

    @Test
    void shouldRemoveTransientFields() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "hasC2Applications", "some data",
                "isMessageRegardingC2", "some data",
                "c2DynamicList", "some data",
                "relatedDocumentsLabel", "some data",
                "nextHearingLabel", "some data",
                "judicialMessageMetaData", JudicialMessageMetaData.builder()
                    .recipient("some data")
                    .sender("some data")
                    .urgency("some data")
                    .build(),
                "judicialMessageNote", "some data",
                "judicialMessageDynamicList", buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, "some data")),
                "messageJudgeOption", MessageJudgeOptions.REPLY,
                "judicialMessageReply", JudicialMessage.builder().build()
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getData()).doesNotContainKeys("hasC2Applications",
            "isMessageRegardingC2",
            "c2DynamicList",
            "relatedDocumentsLabel",
            "nextHearingLabel",
            "judicialMessageMetaData",
            "judicialMessageNote",
            "judicialMessageDynamicList",
            "messageJudgeOption",
            "judicialMessageReply"
        );
    }
}
