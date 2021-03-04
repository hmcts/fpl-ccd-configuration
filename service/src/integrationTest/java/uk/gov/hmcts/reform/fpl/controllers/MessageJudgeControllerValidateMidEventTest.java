package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.MessageJudgeOptions;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessageMetaData;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

@WebMvcTest(MessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class MessageJudgeControllerValidateMidEventTest extends AbstractCallbackTest {
    private static final String SENDER = "ben@fpla.com";
    private static final String MESSAGE = "Some message";
    private static final String REPLY = "Some reply";
    private static final String MESSAGE_REQUESTED_BY = "request review from some court";
    private static final String MESSAGE_RECIPIENT = "recipient@fpla.com";
    private static final UUID SELECTED_DYNAMIC_LIST_ITEM_ID = UUID.randomUUID();

    MessageJudgeControllerValidateMidEventTest() {
        super("message-judge");
    }

    @Test
    void shouldReturnValidationErrorWhenJudgeMessageReplyHaveSameSenderAndRecipientEmailsAddress() {
        String dateSent = formatLocalDateTimeBaseUsingFormat(now().minusDays(1), DATE_TIME_AT);

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageDynamicList(buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, dateSent)))
            .judicialMessageReply(JudicialMessage.builder()
                .isReplying(YesNo.YES.getValue())
                .latestMessage(REPLY)
                .replyFrom(SENDER)
                .replyTo(SENDER)
                .build())
            .messageJudgeOption(MessageJudgeOptions.REPLY)
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(List.of(
                element(SELECTED_DYNAMIC_LIST_ITEM_ID, buildJudicialMessage(dateSent))))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate");

        assertThat(callbackResponse.getErrors())
            .containsOnly("The sender's and recipient's email address cannot be the same");
    }

    @Test
    void shouldNotValidateEmailAddressesWhenClosingAJudicialMessage() {
        String dateSent = formatLocalDateTimeBaseUsingFormat(now().minusDays(1), DATE_TIME_AT);

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageDynamicList(buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, dateSent)))
            .judicialMessageReply(JudicialMessage.builder()
                .isReplying(YesNo.NO.getValue())
                .latestMessage(REPLY)
                .build())
            .messageJudgeOption(MessageJudgeOptions.REPLY)
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(List.of(
                element(SELECTED_DYNAMIC_LIST_ITEM_ID, buildJudicialMessage(dateSent))))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldNotValidateWhenInitiatingANewJudicialMessage() {
        String dateSent = formatLocalDateTimeBaseUsingFormat(now().minusDays(1), DATE_TIME_AT);

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageDynamicList(buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, dateSent)))
            .judicialMessageMetaData(JudicialMessageMetaData.builder()
                .sender(SENDER)
                .recipient(MESSAGE_RECIPIENT)
                .build())
            .judicialMessageNote("new message")
            .messageJudgeOption(MessageJudgeOptions.NEW_MESSAGE)
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(List.of(
                element(SELECTED_DYNAMIC_LIST_ITEM_ID, buildJudicialMessage(dateSent))))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    private JudicialMessage buildJudicialMessage(String dateSent) {
        return JudicialMessage.builder()
            .sender(SENDER)
            .recipient(MESSAGE_RECIPIENT)
            .updatedTime(now().minusDays(2))
            .status(OPEN)
            .subject(MESSAGE_REQUESTED_BY)
            .latestMessage(MESSAGE)
            .messageHistory(String.format("%s - %s", SENDER, MESSAGE))
            .dateSent(dateSent)
            .build();
    }
}

