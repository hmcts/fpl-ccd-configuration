package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.WHICH_ORDERS;

class WhichCareOrderValidatorTest {

    private static final String SELECT_ORDER_MESSAGE = "Select a care order to be discharged";
    private static final String SELECT_ONE_ORDER_MESSAGE = "Select one care order to discharge";

    private final WhichCareOrderValidator underTest = new WhichCareOrderValidator();

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(WHICH_ORDERS);
    }

    @Test
    void validateOneCareOrderSelected() {
        CaseData caseData = CaseData.builder()
            .careOrderSelector(Selector.builder().selected(List.of(0)).build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void validateMultipleCareOrderSelected() {
        CaseData caseData = CaseData.builder()
            .careOrderSelector(Selector.builder().selected(List.of(1, 2)).build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(SELECT_ONE_ORDER_MESSAGE));
    }

    @Test
    void validateNoCareOrdersSelected() {
        CaseData caseData = CaseData.builder()
            .careOrderSelector(Selector.builder().build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(SELECT_ORDER_MESSAGE));
    }

    @Test
    void validateNoCareOrders() {
        CaseData caseData = CaseData.builder()
            .careOrderSelector(Selector.builder()
                .count("0")
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(SELECT_ORDER_MESSAGE));
    }
}
