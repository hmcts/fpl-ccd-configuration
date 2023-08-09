package uk.gov.hmcts.reform.fpl.validation.validators;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.validation.AbstractValidationTest;
import uk.gov.hmcts.reform.fpl.validation.groups.MigrateStateGroup;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.State.FINAL_HEARING;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class IsStateMigratableValidatorTest extends AbstractValidationTest {
    private static final String ERROR_MESSAGE = "Final orders have been issued in this case. You must remove the"
        + " relevant orders before changing the case state.";

    @Test
    void shouldReturnAnErrorWhenAChildContainsAFinalOrder() {
        CaseData caseData = CaseData.builder()
            .state(CLOSED)
            .children1(wrapElements(
                Child.builder()
                    .finalOrderIssued("Yes")
                    .build(),
                Child.builder().build()))
            .build();

        List<String> validationErrors = validate(caseData, MigrateStateGroup.class);
        assertThat(validationErrors).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorWhenAChildContainsBothFinalOrderProperties() {
        CaseData caseData = CaseData.builder()
            .state(CLOSED)
            .children1(wrapElements(
                Child.builder()
                    .finalOrderIssued("Yes")
                    .finalOrderIssuedType("Some data")
                    .build(),
                Child.builder().build()))
            .build();

        List<String> validationErrors = validate(caseData, MigrateStateGroup.class);
        assertThat(validationErrors).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorWhenChildrenDoNotHaveFinalOrderProperties() {
        CaseData caseData = CaseData.builder()
            .state(CLOSED)
            .children1(wrapElements(
                Child.builder()
                    .party(ChildParty.builder()
                        .gender("Male")
                        .build())
                    .build()))
            .build();

        List<String> validationErrors = validate(caseData, MigrateStateGroup.class);
        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldNotReturnAnErrorWhenChildrenHaveFinalOrderAsNo() {
        CaseData caseData = CaseData.builder()
            .state(CLOSED)
            .children1(wrapElements(
                Child.builder()
                    .finalOrderIssued("No")
                    .party(ChildParty.builder()
                        .gender("Male")
                        .build())
                    .build()))
            .build();

        List<String> validationErrors = validate(caseData, MigrateStateGroup.class);
        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenStateIsNotClosed() {
        CaseData caseData = CaseData.builder()
            .state(FINAL_HEARING)
            .children1(wrapElements(
                Child.builder()
                    .party(ChildParty.builder()
                        .gender("Male")
                        .build())
                    .build()))
            .build();

        List<String> validationErrors = validate(caseData, MigrateStateGroup.class);
        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldNotReturnAnErrorWhenChildrenDoesNotExistOnCaseData() {
        List<String> validationErrors = validate(CaseData.builder().build(), MigrateStateGroup.class);
        assertThat(validationErrors).isEmpty();
    }
}
