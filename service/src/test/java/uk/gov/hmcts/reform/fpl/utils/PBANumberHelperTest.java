package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PBANumberHelperTest {

    @Test
    public void given7DigitNumber_whenIEnterIt_thenPBAIsAddedToTheStart() {
        // given 7 digit number
        String input = "1234567";

        // when I enter it
        String actual = PBANumberHelper.updatePBANumber(input);

        // then PBA is added to the start of number.
        assertThat(actual).isEqualTo("PBA1234567");
    }

    @Test
    public void givenNumberStartingWithPBAAndA7DigitNumber_whenIEnterIt_thenPBAIsNotAddedToTheStart() {
        // given 7 digit number with PBA at the start
        String input = "PBA1234567";

        // when I enter it
        String actual = PBANumberHelper.updatePBANumber(input);

        // then PBA is unchanged.
        assertThat(actual).isEqualTo("PBA1234567");
    }

    @Test
    public void givenNumberStartingWithpbaAndA7DigitNumber_whenIEnterIt_thenPBAIsAddedToTheStart() {
        // given 7 digit number
        String input = "pba1234567";

        // when I enter it
        String actual = PBANumberHelper.updatePBANumber(input);

        // then PBA has PBA added to the start.
        assertThat(actual).isEqualTo("PBApba1234567");
    }

    @Test
    public void givenPBAAnd7DigitNumber_whenIValidateIt_thenThereAreNoValidationErrors() {
        // given PBA and a 7 digit number
        String input = "PBA1234567";

        // when I enter it
        List<String> actual = PBANumberHelper.validatePBANumber(input);

        // then PBA number is valid and there are no validation errors
        assertThat(actual).isEmpty();
    }

    @Test
    public void givenpbaAnd7DigitNumber_whenIValidateIt_thenThereAreNoValidationErrors() {
        // given pba and a 7 digit number
        String input = "pba1234567";

        // when I enter it
        List<String> actual = PBANumberHelper.validatePBANumber(input);

        // then PBA number is valid and there are no validation errors
        assertThat(actual).isEmpty();
    }

    @Test
    public void givenPBAAndSomeOtherTextAnd7DigitNumber_whenIValidateIt_thenThereIsValidationError() {
        // given PBA and a 7 digit number
        String input = "PBApba1234567";

        // when I enter it
        List<String> actual = PBANumberHelper.validatePBANumber(input);

        // then PBA number is not valid and there is a validation error
        assertThat(actual).isNotEmpty();
        assertThat(actual).containsExactly("Payment by account (PBA) number must include 7 numbers");
    }

    @Test
    public void givenPBAAnd6DigitNumber_whenIValidateIt_thenThereIsValidationError() {
        // given PBA and a 6 digit number
        String input = "PBA123456";

        // when I enter it
        List<String> actual = PBANumberHelper.validatePBANumber(input);

        // then PBA number is not valid and there is a validation error
        assertThat(actual).isNotEmpty();
        assertThat(actual).containsExactly("Payment by account (PBA) number must include 7 numbers");
    }

    @Test
    public void givenPBAAnd8DigitNumber_whenIValidateIt_thenThereIsValidationError() {
        // given PBA and a 8 digit number
        String input = "PBA12345678";

        // when I enter it
        List<String> actual = PBANumberHelper.validatePBANumber(input);

        // then PBA number is not valid and there is a validation error
        assertThat(actual).isNotEmpty();
        assertThat(actual).containsExactly("Payment by account (PBA) number must include 7 numbers");
    }

    @Test
    public void givenPBAAndNotADigit_whenIValidateIt_thenThereIsValidationError() {
        // given PBA and text in the digit number
        String input = "PBA1E2345678";

        // when I enter it
        List<String> actual = PBANumberHelper.validatePBANumber(input);

        // then PBA number is not valid and there is a validation error
        assertThat(actual).isNotEmpty();
        assertThat(actual).containsExactly("Payment by account (PBA) number must include 7 numbers");
    }


}
