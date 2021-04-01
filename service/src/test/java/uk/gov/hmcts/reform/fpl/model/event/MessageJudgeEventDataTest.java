package uk.gov.hmcts.reform.fpl.model.event;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.MessageJudgeOptions;

import static org.assertj.core.api.Assertions.assertThat;

class MessageJudgeEventDataTest {
    @Test
    void shouldReturnTrueWhenIsReplyingToAMessage() {
        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .messageJudgeOption(MessageJudgeOptions.REPLY)
            .build();

        assertThat(messageJudgeEventData.isReplyingToAMessage()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNotReplyingToAMessage() {
        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .messageJudgeOption(MessageJudgeOptions.NEW_MESSAGE)
            .build();

        assertThat(messageJudgeEventData.isReplyingToAMessage()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenMessageOptionIsNotPopulated() {
        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder().build();

        assertThat(messageJudgeEventData.isReplyingToAMessage()).isFalse();
    }
}
