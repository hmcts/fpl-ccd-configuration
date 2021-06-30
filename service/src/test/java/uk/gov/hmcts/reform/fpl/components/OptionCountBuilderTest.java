package uk.gov.hmcts.reform.fpl.components;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


class OptionCountBuilderTest {

    private final OptionCountBuilder underTest = new OptionCountBuilder();

    @Test
    void testNullValue() {
        String actual = underTest.generateCode(null);

        assertThat(actual).isEqualTo("");
    }

    @Test
    void testEmptyInstance() {
        String actual = underTest.generateCode(List.of());

        assertThat(actual).isEqualTo("");
    }

    @Test
    void testSingleInstance() {
        String actual = underTest.generateCode(List.of(new Object()));

        assertThat(actual).isEqualTo("0");
    }

    @Test
    void testMultipleInstance() {
        String actual = underTest.generateCode(List.of(new Object(), new Object(), new Object()));

        assertThat(actual).isEqualTo("012");
    }

}
