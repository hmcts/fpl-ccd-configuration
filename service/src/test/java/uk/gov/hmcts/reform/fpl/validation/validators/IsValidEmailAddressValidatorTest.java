package uk.gov.hmcts.reform.fpl.validation.validators;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.validation.AbstractValidationTest;
import uk.gov.hmcts.reform.fpl.validation.groups.ValidEmailGroup;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class IsValidEmailAddressValidatorTest extends AbstractValidationTest {
    private ValidateEmailService validateEmailService;

    private static final String ERROR_MESSAGE = "Enter an email address in the correct format,"
        + " for example name@example.com";

    @ParameterizedTest
    @MethodSource("validEmailAddresses")
    void shouldNotReturnAnErrorWhenEmailIsFormattedCorrectly(String emailAddress) {
        List<String> validationErrors = validate(
            EmailAddress.builder().email(emailAddress).build(), ValidEmailGroup.class);

        assertThat(validationErrors).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidEmailAddresses")
    void shouldReturnAnErrorWhenEmailIsFormattedIncorrectly(String emailAddress) {
        List<String> validationErrors = validate(
            EmailAddress.builder().email(emailAddress).build(), ValidEmailGroup.class);

        assertThat(validationErrors).contains(ERROR_MESSAGE);
    }

    private static Stream<Arguments> validEmailAddresses() {
        return Stream.of(
            "email@example.com",
            "firstname.lastname@example.com",
            "email@subdomain.example.com",
            "firstname+lastname@example.com",
            "email@123.123.123.123",
            "email@example.name")
            .map(Arguments::of);
    }

    private static Stream<Arguments> invalidEmailAddresses() {
        return Stream.of(
            "<John Doe> johndoe@email.com",
            "very.unusual.”@”.unusual.com@example.com",
            "very.”(),:;<>[]”.VERY.”very@\\\\ \"very”.unusual@strange.example.com")
            .map(Arguments::of);
    }
}
