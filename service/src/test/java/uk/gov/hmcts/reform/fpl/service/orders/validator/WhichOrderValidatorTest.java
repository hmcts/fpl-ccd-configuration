package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.WHICH_ORDERS;

class WhichOrderValidatorTest {

    private static final String MESSAGE = "Select care orders to be discharged";

    private final WhichOrderValidator underTest = new WhichOrderValidator();

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(WHICH_ORDERS);
    }

    @Test
    void validateSomeOrdersSelected() {
        CaseData caseData = CaseData.builder()
            .careOrderSelector(Selector.builder().selected(List.of(1, 2)).build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void validateNoOrdersSelected() {
        CaseData caseData = CaseData.builder()
            .orderAppliesToAllChildren("No")
            .careOrderSelector(Selector.builder().build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(MESSAGE));
    }

    @Test
    void validateNoCareOrdersSelected() {
        CaseData caseData = CaseData.builder()
            .careOrderSelector(Selector.builder().build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(MESSAGE));
    }

    @Test
    void validateNoCareOrders() {
        CaseData caseData = CaseData.builder()
            .careOrderSelector(Selector.builder()
                .count("0")
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(MESSAGE));
    }
}
