package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPOINTED_GUARDIAN;

class AppointedGuardianValidatorTest {

    private static final String MESSAGE = "Select the appointed guardian for the children from the"
        + " list of parties or detail the special guardians in the free text field. ";

    private final AppointedGuardianValidator underTest = new AppointedGuardianValidator();

    //@Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(APPOINTED_GUARDIAN);
    }

    //@Test
    void validateAnyGuardianSelected() {
        CaseData caseData = CaseData.builder()
            .orderAppliesToAllChildren("No")
            .appointedGuardianSelector(Selector.builder().selected(List.of(1, 2)).build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    //@Test
    void validatePartiesSpecifiedInTextField() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .additionalAppointedSpecialGuardians("Joe Bloggs")
                .build())
            .appointedGuardianSelector(Selector.builder().build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    //@Test
    void validateNoGuardianSelected() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .build())
            .appointedGuardianSelector(Selector.builder().build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(MESSAGE));
    }
}
