package uk.gov.hmcts.reform.fpl.validation.validators;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.validation.AbstractValidationTest;
import uk.gov.hmcts.reform.fpl.validation.groups.MigrateClosedCaseGroup;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class HasRemainingFinalOrdersValidatorTest extends AbstractValidationTest {
    private static final String ERROR_MESSAGE = "Final orders have been issued in this case. You must remove the"
        + " relevant orders before changing the case state.";

    @Test
    void shouldReturnAnErrorWhenAChildContainsAFinalOrderProperty() {
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(
                Child.builder()
                    .finalOrderIssued("Some data")
                    .build(),
                Child.builder().build()))
            .build();

        List<String> validationErrors = validate(caseData, MigrateClosedCaseGroup.class);
        assertThat(validationErrors).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorWhenAChildContainsBothFinalOrderProperties() {
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(
                Child.builder()
                    .finalOrderIssued("Some data")
                    .finalOrderIssuedType("Some data")
                    .build(),
                Child.builder().build()))
            .build();

        List<String> validationErrors = validate(caseData, MigrateClosedCaseGroup.class);
        assertThat(validationErrors).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorWhenChildrenDoNotHaveFinalOrderProperties() {
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(
                Child.builder()
                    .party(ChildParty.builder()
                        .gender("Male")
                        .build())
                    .build()))
            .build();

        List<String> validationErrors = validate(caseData, MigrateClosedCaseGroup.class);
        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldNotReturnAnErrorWhenChildrenDoesNotExistOnCaseData() {
        List<String> validationErrors = validate(CaseData.builder().build(), MigrateClosedCaseGroup.class);
        assertThat(validationErrors).isEmpty();
    }
}
