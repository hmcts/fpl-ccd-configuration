package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

class JudicialMessageTest {
    private static String DATE_SENT = "11 November at 08:30am";
    private static String URGENCY = "High urgency";
    private static String SUBJECT = "Subject";

    @Test
    void shouldBuildJudicialMessageLabel() {
        JudicialMessage judicialMessage = JudicialMessage.builder()
            .urgency(URGENCY)
            .isRelatedToC2(YES)
            .dateSent(DATE_SENT)
            .subject(SUBJECT)
            .build();

        assertThat(judicialMessage.toLabel()).isEqualTo(String.format("C2, %s, %s, %s", SUBJECT, DATE_SENT, URGENCY));
    }

    @Test
    void shouldBuildJudicialMessageLabelWithoutC2() {
        JudicialMessage judicialMessage = JudicialMessage.builder()
            .urgency(URGENCY)
            .dateSent(DATE_SENT)
            .subject(SUBJECT)
            .build();

        assertThat(judicialMessage.toLabel()).isEqualTo(String.format("%s, %s, %s", SUBJECT, DATE_SENT, URGENCY));
    }

    @Test
    void shouldBuildJudicialMessageLabelWithoutUrgency() {
        JudicialMessage judicialMessage = JudicialMessage.builder()
            .isRelatedToC2(YES)
            .dateSent(DATE_SENT)
            .build();

        assertThat(judicialMessage.toLabel()).isEqualTo(String.format("C2, %s", DATE_SENT));
    }

    @Test
    void shouldBuildJudicialMessageLabelWithoutUrgencyIfUrgencyIsBlankString() {
        JudicialMessage judicialMessage = JudicialMessage.builder()
            .urgency(" ")
            .dateSent(DATE_SENT)
            .build();

        assertThat(judicialMessage.toLabel()).isEqualTo(String.format("%s", DATE_SENT));
    }

    @Test
    void shouldBuildJudicialMessageLabelWithMaximumLengthAllowedWhenUrgencyIsTooLong() {
        String longUrgency = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed sollicitudin eu felis "
            + "tincidunt volutpat. Donec tempus quis metus congue placerat. Sed ligula nisl, tempor at eleifend ac, "
            + "consequat condimentum sem. In sed porttitor turpis, at laoreet quam. Fusce bibendum vehicula ipsum, et "
            + "tempus ante fermentum non.";

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .urgency(longUrgency.substring(0, 249))
            .dateSent(DATE_SENT)
            .build();

        String truncatedUrgencyText = "Lorem ipsum dolor sit amet, consectetur adipiscing "
            + "elit. Sed sollicitudin eu felis tincidunt volutpat. Donec tempus quis metus congue placerat. Sed ligula "
            + "nisl, tempor at eleifend ac, consequat condimentum sem. In sed portt...";

        String expectedMessageLabel = String.format("%s, %s", DATE_SENT, truncatedUrgencyText);

        assertThat(judicialMessage.toLabel()).isEqualTo(expectedMessageLabel);
    }

    @Test
    void shouldBuildJudicialMessageLabelWithOnlyDateSent() {
        JudicialMessage judicialMessage = JudicialMessage.builder()
            .dateSent(DATE_SENT)
            .build();

        assertThat(judicialMessage.toLabel()).isEqualTo(DATE_SENT);
    }

    @Test
    void shouldReturnTrueWhenMessageHasNotHadAReply() {
        JudicialMessage judicialMessage = JudicialMessage.builder()
            .latestMessage("This is a new message")
            .sender("test@mail.com")
            .messageHistory("test@mail.com - This is a new message").build();

        assertThat(judicialMessage.isFirstMessage()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenMessageHasHadAReply() {
        JudicialMessage judicialMessage = JudicialMessage.builder()
            .latestMessage("Fix up the order")
            .sender("test2@mail.com")
            .messageHistory("test@mail.com - This is a new message, Fix up the order").build();

        assertThat(judicialMessage.isFirstMessage()).isFalse();
    }
}
