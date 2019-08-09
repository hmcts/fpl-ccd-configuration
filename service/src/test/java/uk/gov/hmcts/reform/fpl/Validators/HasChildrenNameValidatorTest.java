package uk.gov.hmcts.reform.fpl.Validators;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Children;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.validators.HasChildrenNameValidator;

import javax.validation.ConstraintValidatorContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class HasChildrenNameValidatorTest {
    private HasChildrenNameValidator validator = new HasChildrenNameValidator();
    private ConstraintValidatorContext constraintValidatorContext;

    @Test
    void shouldReturnFalseIfChildrenDoesNotExist() {
        Children children = Children.builder().build();
        Boolean isValid = validator.isValid(children, constraintValidatorContext);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnTrueIfFirstChildHasChildName() {
        Children children = Children.builder()
            .firstChild(Child.builder()
                .childName("James")
                .build())
            .build();
        Boolean isValid = validator.isValid(children, constraintValidatorContext);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnTrueIfAdditionalChildHasChildName() {
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
        Boolean isValid = validator.isValid(children, constraintValidatorContext);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnTrueIfBothFirstChildAndAdditionalChildrenHaveChildName() {
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
        Boolean isValid = validator.isValid(children, constraintValidatorContext);

        assertThat(isValid).isTrue();
    }
}
