package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.WHICH_CHILDREN;

class WhichChildrenValidatorTest {

    private static final String MESSAGE = "Select the children included in the order";

    private final WhichChildrenValidator underTest = new WhichChildrenValidator();

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(WHICH_CHILDREN);
    }

    @Test
    void validateOrderAppliesToAllChildren() {
        CaseData caseData = CaseData.builder()
            .orderAppliesToAllChildren("Yes")
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void validateSomeChildrenSelected() {
        CaseData caseData = CaseData.builder()
            .orderAppliesToAllChildren("No")
            .childSelector(Selector.builder().selected(List.of(1, 2)).build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void validateNoChildrenSelected() {
        CaseData caseData = CaseData.builder()
            .orderAppliesToAllChildren("No")
            .childSelector(Selector.builder().build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(MESSAGE));
    }
}
