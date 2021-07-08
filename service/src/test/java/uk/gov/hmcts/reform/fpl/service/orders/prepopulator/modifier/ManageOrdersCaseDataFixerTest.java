package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.modifier;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderOperation;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.Order.AMENED_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderOperation.AMEND;

class ManageOrdersCaseDataFixerTest {

    private static final OrderOperation ORDER_OPERATION = OrderOperation.CREATE;

    private final ManageOrdersCaseDataFixer underTest = new ManageOrdersCaseDataFixer();

    @Test
    void shouldNotModifyIfNotUploadOrClosedOrAmend() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOperation(ORDER_OPERATION)
                .build())
            .build();

        CaseData actual = underTest.fix(caseData);

        assertThat(actual).isEqualTo(caseData);
    }

    @Test
    void setOrderTypeWhenAmendInNonClosed() {
        CaseData actual = underTest.fix(CaseData.builder()
            .state(State.CLOSED)
            .manageOrdersEventData(
                ManageOrdersEventData.builder()
                    .manageOrdersOperation(AMEND)
                    .build()
            ).build());

        assertThat(actual).isEqualTo(CaseData.builder()
            .state(State.CLOSED)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOperation(AMEND)
                .manageOrdersType(AMENED_ORDER)
                .build()
            ).build());
    }

    @Test
    void setOrderTypeWhenAmendInClosed() {
        CaseData actual = underTest.fix(CaseData.builder()
            .state(State.CLOSED)
            .manageOrdersEventData(
                ManageOrdersEventData.builder()
                    .manageOrdersOperationClosedState(AMEND)
                    .build()
            ).build());

        assertThat(actual).isEqualTo(CaseData.builder()
            .state(State.CLOSED)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOperationClosedState(AMEND)
                .manageOrdersType(AMENED_ORDER)
                .build()
            ).build());
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
                .manageOrdersType(C21_BLANK_ORDER)
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
