package uk.gov.hmcts.reform.fpl.validators;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Children;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class HasChildrenNameValidatorTest {
    private Validator validator;

    private static final String ERROR_MESSAGE = "Tell us the names of all children in the case";

    @BeforeEach
    private void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldNotReturnAnErrorIfFirstChildHasChildName() {
        Children children = Children.builder()
            .firstChild(Child.builder()
                .childName("James")
                .build())
            .build();

        List<String> errorMessages = validator.validate(children).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfAdditionalChildHasChildName() {
        Children children = Children.builder()
            .additionalChildren(ImmutableList.of(
                Element.<Child>builder()
                    .id(UUID.randomUUID())
                    .value(Child.builder()
                        .childName("James")
                        .build())
                    .build()
            ))
            .build();

        List<String> errorMessages = validator.validate(children).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfBothFirstChildAndAdditionalChildrenHaveChildName() {
        Children children = Children.builder()
            .firstChild(Child.builder()
                .childName("James")
                .build())
            .additionalChildren(ImmutableList.of(
                Element.<Child>builder()
                    .id(UUID.randomUUID())
                    .value(Child.builder()
                        .childName("James")
                        .build())
                    .build()
            ))
            .build();

        List<String> errorMessages = validator.validate(children).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfChildrenDoesNotExist() {
        Children children = Children.builder().build();

        List<String> errorMessages = validator.validate(children).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfFirstChildNameIsEmptyString() {
        Children children = Children.builder()
            .firstChild(Child.builder()
                .childName("")
                .build())
            .build();

        List<String> errorMessages = validator.validate(children).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfFirstChildHasChildNameButAdditionalChildHasEmptyStringAsChildName() {
        Children children = Children.builder()
            .firstChild(Child.builder()
                .childName("James")
                .build())
            .additionalChildren(ImmutableList.of(
                Element.<Child>builder()
                    .id(UUID.randomUUID())
                    .value(Child.builder()
                        .childName("")
                        .build())
                    .build()
            ))
            .build();

        List<String> errorMessages = validator.validate(children).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }
}
