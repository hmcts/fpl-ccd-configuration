package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ChildrenChecker.class, LocalValidatorFactoryBean.class})
class ChildrenCheckerTest {

    @Autowired
    private ChildrenChecker childrenChecker;

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnErrorWhenNoChildrenSpecified(List<Element<Child>> children) {
        final CaseData caseData = CaseData.builder()
            .children1(children)
            .build();

        final List<String> errors = childrenChecker.validate(caseData);
        final boolean isCompleted = childrenChecker.isCompleted(caseData);

        assertThat(errors).contains("Add the child's details|Ychwanegu manylion y plentyn");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnErrorsWhenNoChildrenDetailsSpecified() {
        final Child child = Child.builder()
                .party(ChildParty.builder().build())
                .build();
        final CaseData caseData = CaseData.builder()
                .children1(ElementUtils.wrapElements(child))
                .build();

        final List<String> errors = childrenChecker.validate(caseData);
        final boolean isCompleted = childrenChecker.isCompleted(caseData);

        assertThat(errors).containsExactlyInAnyOrder(
                "Tell us the names of all children in the case",
                "Tell us the gender of all children in the case",
                "Tell us the date of birth of all children in the case"
        );
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnEmptyErrorsWhenRequiredChildrenDetailsArePresentAndValid() {
        final Child child = Child.builder()
                .party(ChildParty.builder()
                        .firstName("Alex")
                        .lastName("Brown")
                        .gender(ChildGender.BOY)
                        .dateOfBirth(LocalDate.now().minusYears(20))
                        .build())
                .build();
        final CaseData caseData = CaseData.builder()
                .children1(ElementUtils.wrapElements(child))
                .build();

        final List<String> errors = childrenChecker.validate(caseData);
        final boolean isCompleted = childrenChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isTrue();
    }

    @Test
    void shouldReturnErrorWhenDetailsOfNonStandardGenderAreMissing() {
        final Child child = Child.builder()
                .party(ChildParty.builder()
                        .firstName("Alex")
                        .lastName("Brown")
                        .gender(ChildGender.OTHER)
                        .dateOfBirth(LocalDate.now().minusYears(20))
                        .build())
                .build();
        final CaseData caseData = CaseData.builder()
                .children1(ElementUtils.wrapElements(child))
                .build();

        final List<String> errors = childrenChecker.validate(caseData);
        final boolean isCompleted = childrenChecker.isCompleted(caseData);

        assertThat(errors).containsExactly("Tell us the gender of all children in the case");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnErrorWhenDateOfBirthIsInFuture() {
        final Child child = Child.builder()
                .party(ChildParty.builder()
                        .firstName("Alex")
                        .lastName("Brown")
                        .gender(ChildGender.BOY)
                        .dateOfBirth(LocalDate.now().plusDays(1))
                        .build())
                .build();
        final CaseData caseData = CaseData.builder()
                .children1(ElementUtils.wrapElements(child))
                .build();

        final List<String> errors = childrenChecker.validate(caseData);
        final boolean isCompleted = childrenChecker.isCompleted(caseData);

        assertThat(errors)
                .containsExactly("Date of birth is in the future. You cannot send this application until that date");
        assertThat(isCompleted).isFalse();
    }
}
