package uk.gov.hmcts.reform.fpl.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.ChildParty;

import java.util.List;

import java.util.stream.Collectors;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

public class  HasChildNameValidatorTest {
    private Validator validator;

    private static final String ERROR_MESSAGE = "Tell us the names of all children in the case";

    @BeforeEach
    private void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldReturnAnErrorIfChildPartyIsNotPopulated() {
        ChildParty childParty = ChildParty.builder().build();

        List<String> errorMessages = validator.validate(childParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfFirstNameIsBlank() {
        ChildParty childParty = ChildParty.builder()
            .firstName("")
            .build();

        List<String> errorMessages = validator.validate(childParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfLastNameIsBlank() {
        ChildParty childParty = ChildParty.builder()
            .lastName("")
            .build();

        List<String> errorMessages = validator.validate(childParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfBothLastNameAndFirstNameIsBlank() {
        ChildParty childParty = ChildParty.builder()
            .firstName("")
            .lastName("")
            .build();

        List<String> errorMessages = validator.validate(childParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfFirstNameIsEntered() {
        ChildParty childParty = ChildParty.builder()
            .firstName("James")
            .build();

        List<String> errorMessages = validator.validate(childParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfLastNameIsEntered() {
        ChildParty childParty = ChildParty.builder()
            .lastName("Burns")
            .build();

        List<String> errorMessages = validator.validate(childParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfFirstNameIsEnteredAndLastNameIsBlank() {
        ChildParty childParty = ChildParty.builder()
            .firstName("James")
            .lastName("")
            .build();

        List<String> errorMessages = validator.validate(childParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfFirstNameAndLastNameIsEntered() {
        ChildParty childParty = ChildParty.builder()
            .firstName("James")
            .lastName("Burns")
            .build();

        List<String> errorMessages = validator.validate(childParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }
}
