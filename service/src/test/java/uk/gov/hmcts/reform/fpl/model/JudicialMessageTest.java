package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

class JudicialMessageTest {
    private static String DATE_SENT = "11 November at 08:30am";
    private static String URGENCY = "High urgency";

    @Test
    void shouldBuildJudicialMessageLabel() {
        JudicialMessage judicialMessage = JudicialMessage.builder()
            .urgency(URGENCY)
            .isRelatedToC2(YES)
            .dateSent(DATE_SENT)
            .build();

        assertThat(judicialMessage.toLabel()).isEqualTo(String.format("C2, %s, %s", URGENCY, DATE_SENT));
    }

    @Test
    void shouldBuildJudicialMessageLabelWithoutC2() {
        JudicialMessage judicialMessage = JudicialMessage.builder()
            .urgency(URGENCY)
            .dateSent(DATE_SENT)
            .build();

        assertThat(judicialMessage.toLabel()).isEqualTo(String.format("%s, %s", URGENCY, DATE_SENT));
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
    void shouldBuildJudicialMessageLabelWithOnlyDateSent() {
        JudicialMessage judicialMessage = JudicialMessage.builder()
            .dateSent(DATE_SENT)
            .build();

        assertThat(judicialMessage.toLabel()).isEqualTo(DATE_SENT);
    }
}
