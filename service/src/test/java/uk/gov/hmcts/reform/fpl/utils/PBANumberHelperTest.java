package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PBANumberHelperTest {

    @Test
    void givenThatA7DigitNumber_whenIEnterIt_thenPBAIsAddedToTheStart() {
        // given 7 digit number
        String input = "1234567";

        // when I enter it
        String actual = PBANumberHelper.updatePBANumber(input);

        // then PBA is added to the start of  number.
        assertThat(actual).isEqualTo("PBA1234567");
    }

    @Test
    void givenThatANumberStartingWithPBAAndA7DigitNumber_whenIEnterIt_thenPBAIsNotAddedToTheStart() {
        // given 7 digit number with PBA at the start
        String input = "PBA1234567";

        // when I enter it
        String actual = PBANumberHelper.updatePBANumber(input);

        // then PBA is unchanged.
        assertThat(actual).isEqualTo("PBA1234567");
    }

    @Test
    void givenThatANumberStartingWithpbaAndA7DigitNumber_whenIEnterIt_thenPBAIsAddedToTheStart() {
        // given 7 digit number
        String input = "pba1234567";

        // when I enter it
        String actual = PBANumberHelper.updatePBANumber(input);

        // then PBA is unchanged.
        assertThat(actual).isEqualTo("PBApba1234567");
    }
}
