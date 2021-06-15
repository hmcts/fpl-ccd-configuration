package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.modifier;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderOperation;

import static org.assertj.core.api.Assertions.assertThat;

class ManageOrdersCaseDataFixerTest {

    private static final OrderOperation ORDER_OPERATION = OrderOperation.CREATE;

    private final ManageOrdersCaseDataFixer underTest = new ManageOrdersCaseDataFixer();

    @Test
    void shouldNotModifyIfNotClosedState() {
        CaseData actual = underTest.fix(CaseData.builder().build());

        assertThat(actual).isEqualTo(CaseData.builder().build());
    }

    @Test
    void defaultBlankAndOrderStateOrderIfInClosedState() {
        CaseData actual = underTest.fix(CaseData.builder()
            .state(State.CLOSED)
            .manageOrdersEventData(
                ManageOrdersEventData.builder()
                    .manageOrdersOperationClosedState(ORDER_OPERATION)
                    .build()
            ).build());

        assertThat(actual).isEqualTo(CaseData.builder()
            .state(State.CLOSED)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOperationClosedState(ORDER_OPERATION)
                .manageOrdersOperation(ORDER_OPERATION)
                .manageOrdersType(Order.C21_BLANK_ORDER)
                .build()
            ).build());
    }

    @Test
    void fixHiddentTypeInCaseUploadFlow() {
        CaseData actual = underTest.fix(CaseData.builder().manageOrdersEventData(
            ManageOrdersEventData.builder()
                .manageOrdersOperation(OrderOperation.UPLOAD)
                .manageOrdersUploadType(Order.C28_WARRANT_TO_ASSIST)
                .build()
        ).build());

        assertThat(actual).isEqualTo(CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOperation(OrderOperation.UPLOAD)
                .manageOrdersType(Order.C28_WARRANT_TO_ASSIST)
                .manageOrdersUploadType(Order.C28_WARRANT_TO_ASSIST)
                .build())
            .build());
    }
}
