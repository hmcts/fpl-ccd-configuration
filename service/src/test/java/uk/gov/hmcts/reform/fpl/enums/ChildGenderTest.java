package uk.gov.hmcts.reform.fpl.enums;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ChildGender.BOY;
import static uk.gov.hmcts.reform.fpl.enums.ChildGender.GIRL;
import static uk.gov.hmcts.reform.fpl.enums.ChildGender.OTHER;

public class ChildGenderTest {

    @ParameterizedTest
    @ValueSource(strings = {"boy", "BOY", "Boy"})
    public void shouldReturnBoy(String value) {
        assertThat(ChildGender.fromLabel(value)).isEqualTo(BOY);
    }

    @ParameterizedTest
    @ValueSource(strings = {"girl", "GIRL", "Girl"})
    public void shouldReturnGirl(String value) {
        assertThat(ChildGender.fromLabel(value)).isEqualTo(GIRL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"other", "unknown", " "})
    public void shouldReturnOther(String value) {
        assertThat(ChildGender.fromLabel(value)).isEqualTo(OTHER);
    }
}
