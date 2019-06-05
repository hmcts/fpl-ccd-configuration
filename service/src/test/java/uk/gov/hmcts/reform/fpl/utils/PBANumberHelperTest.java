package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PBANumberHelperTest {

    @Test
    public void given7DigitNumber_whenIEnterIt_thenPBAIsAddedToTheStart() {
        String input = "1234567";

        String actual = PBANumberHelper.updatePBANumber(input);

        assertThat(actual).isEqualTo("PBA1234567");
    }

    @Test
    public void givenNumberStartingWithPBAAndA7DigitNumber_whenIEnterIt_thenPBAIsNotAddedToTheStart() {
        String input = "PBA1234567";

        String actual = PBANumberHelper.updatePBANumber(input);

        assertThat(actual).isEqualTo("PBA1234567");
    }

    @Test
    public void givenNumberStartingWithpbaAndA7DigitNumber_whenIEnterIt_thenPBAIsAddedToTheStart() {
        String input = "pba1234567";

        String actual = PBANumberHelper.updatePBANumber(input);

        assertThat(actual).isEqualTo("PBA1234567");
    }

    @Test
    public void givenPBAAnd7DigitNumber_whenIValidateIt_thenThereAreNoValidationErrors() {
        String input = "PBA1234567";

        List<String> actual = PBANumberHelper.validatePBANumber(input);

        assertThat(actual).isEmpty();
    }

    @Test
    public void givenpbaAnd7DigitNumber_whenIValidateIt_thenThereAreNoValidationErrors() {
        String input = "pba1234567";

        List<String> actual = PBANumberHelper.validatePBANumber(input);

        assertThat(actual).isEmpty();
    }

    @Test
    public void givenPBAAndSomeOtherTextAnd7DigitNumber_whenIValidateIt_thenThereIsValidationError() {
        String input = "PBApba1234567";

        List<String> actual = PBANumberHelper.validatePBANumber(input);

        assertThat(actual).isNotEmpty();
        assertThat(actual).containsExactly("Payment by account (PBA) number must include 7 numbers");
    }

    @Test
    public void givenPBAAnd6DigitNumber_whenIValidateIt_thenThereIsValidationError() {
        String input = "PBA123456";

        List<String> actual = PBANumberHelper.validatePBANumber(input);

        assertThat(actual).isNotEmpty();
        assertThat(actual).containsExactly("Payment by account (PBA) number must include 7 numbers");
    }

    @Test
    public void givenPBAAnd8DigitNumber_whenIValidateIt_thenThereIsValidationError() {
        String input = "PBA12345678";

        List<String> actual = PBANumberHelper.validatePBANumber(input);

        assertThat(actual).isNotEmpty();
        assertThat(actual).containsExactly("Payment by account (PBA) number must include 7 numbers");
    }

    @Test
    public void givenPBAAndNotADigit_whenIValidateIt_thenThereIsValidationError() {
        String input = "PBA1E2345678";

        List<String> actual = PBANumberHelper.validatePBANumber(input);

        assertThat(actual).isNotEmpty();
        assertThat(actual).containsExactly("Payment by account (PBA) number must include 7 numbers");
    }
}
