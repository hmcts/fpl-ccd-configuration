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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ValidateEmailService.class })
class ValidateEmailServiceTest {

    @Autowired
    private ValidateEmailService validateEmailService;

    private static final String ERROR_MESSAGE = "Enter a valid email address";

    @Test
    void shouldReturnAListOfErrorMessagesWhenListContainsSomeInvalidEmailAddresses() {
        List<String> emailAddresses = List.of(
            "email@example.com",
            "<John Doe> johndoe@email.com",
            "firstname.lastname@example.com",
            "very.unusual.”@”.unusual.com@example.com");

        List<String> errorMessages = validateEmailService.validate(emailAddresses, "Gatekeeper");

        assertThat(errorMessages).contains(
            "Gatekeeper 2: Enter a valid email address",
            "Gatekeeper 4: Enter a valid email address");
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
        assertThat(validateEmailService.validate(email)).isEqualTo(ERROR_MESSAGE);
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
