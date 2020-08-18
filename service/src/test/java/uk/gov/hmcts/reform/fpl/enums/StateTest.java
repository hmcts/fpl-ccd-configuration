package uk.gov.hmcts.reform.fpl.enums;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

public class StateTest {

    @Test
    void shouldMapValueToStateEnum() {
        assertThat(State.fromValue("OPEN")).isEqualTo(State.OPEN);
        assertThat(State.fromValue("SUBMITTED")).isEqualTo(State.SUBMITTED);
        assertThat(State.fromValue("GATEKEEPING")).isEqualTo(State.GATEKEEPING);
        assertThat(State.fromValue("PREPARE_FOR_HEARING")).isEqualTo(State.CASE_MANAGEMENT);
        assertThat(State.fromValue("CLOSED")).isEqualTo(State.CLOSED);
        assertThat(State.fromValue("DELETED")).isEqualTo(State.DELETED);
        assertThat(State.fromValue("RETURNED")).isEqualTo(State.RETURNED);
    }

    @Test
    void shouldThrowAnExceptionWhenAttemptingToMapAnInvalidStateValue() {
        Assertions.assertThatThrownBy(() -> State.fromValue("NOT_A_REAL_STATE"))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("Unable to map NOT_A_REAL_STATE to a case state");
    }
}
