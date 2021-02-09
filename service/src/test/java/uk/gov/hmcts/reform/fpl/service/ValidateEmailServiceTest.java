package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ValidateEmailService.class })
class ValidateEmailServiceTest {

    @Autowired
    private ValidateEmailService validateEmailService;

    private static final String ERROR_MESSAGE = "Enter an email address in the correct format, "
        + "for example name@example.com";

    @Test
    void shouldReturnAListOfErrorMessagesWhenListContainsSomeInvalidEmailAddresses() {
        List<String> emailAddresses = List.of(
            "email@example.com",
            "<John Doe> johndoe@email.com",
            "firstname.lastname@example.com",
            "very.unusual.”@”.unusual.com@example.com");

        List<String> errorMessages = validateEmailService.validate(emailAddresses, "Gatekeeper");

        assertThat(errorMessages).contains(
            "Gatekeeper 2: Enter an email address in the correct format, for example name@example.com",
            "Gatekeeper 4: Enter an email address in the correct format, for example name@example.com");
    }

    @Test
    void shouldReturnAnEmptyListWhenListContainsAllValidEmailAddresses() {
        List<String> emailAddresses = List.of(
            "email@example.com",
            "firstname.lastname@example.com");

        List<String> errorMessages = validateEmailService.validate(emailAddresses, "Gatekeeper");

        assertThat(errorMessages).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("validEmailAddresses")
    void shouldNotReturnAnErrorMessageIfEmailIsInvalid(String email) {
        assertThat(validateEmailService.validate(email)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidEmailAddresses")
    void shouldReturnAnErrorMessageIfEmailIsInvalid(String email) {
        assertThat(validateEmailService.validate(email).get()).isEqualTo(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnCustomizedErrorMessageWhenEmailAddressIsInvalid() {
        String email = "<John Doe> johndoe@email.com";

        Optional<String> error = validateEmailService.validate(email, "Custom error message");

        assertThat(error.get()).contains(
            "Custom error message");
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
