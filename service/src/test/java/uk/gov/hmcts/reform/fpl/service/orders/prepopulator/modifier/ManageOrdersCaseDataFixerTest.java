package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.modifier;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.Order.AMENED_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderOperation.AMEND;
import static uk.gov.hmcts.reform.fpl.model.order.OrderOperation.CREATE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderOperation.UPLOAD;

class ManageOrdersCaseDataFixerTest {

    private final ManageOrdersCaseDataFixer underTest = new ManageOrdersCaseDataFixer();

    @Test
    void shouldNotModifyIfNotUploadOrClosedOrAmend() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOperation(CREATE)
                .build())
            .build();

        CaseData actual = underTest.fix(caseData);

        assertThat(actual).isEqualTo(caseData);
    }

    @Test
    void setOrderTypeWhenAmendInNonClosed() {
        CaseData actual = underTest.fix(CaseData.builder()
            .state(State.SUBMITTED)
            .manageOrdersEventData(
                ManageOrdersEventData.builder()
                    .manageOrdersOperation(AMEND)
                    .build()
            ).build());

        assertThat(actual).isEqualTo(CaseData.builder()
            .state(State.SUBMITTED)
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
    void defaultBlankAndOrderStateOrderIfInClosedStateAndCreating() {
        CaseData actual = underTest.fix(CaseData.builder()
            .state(State.CLOSED)
            .manageOrdersEventData(
                ManageOrdersEventData.builder()
                    .manageOrdersOperationClosedState(CREATE)
                    .build()
            ).build());

        assertThat(actual).isEqualTo(CaseData.builder()
            .state(State.CLOSED)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOperationClosedState(CREATE)
                .manageOrdersOperation(CREATE)
                .manageOrdersType(C21_BLANK_ORDER)
                .build()
            ).build());
    }

    @Test
    void fixHiddenTypeInCaseUploadFlow() {
        CaseData actual = underTest.fix(CaseData.builder().manageOrdersEventData(
            ManageOrdersEventData.builder()
                .manageOrdersOperation(UPLOAD)
                .manageOrdersUploadType(Order.C28_WARRANT_TO_ASSIST)
                .build()
        ).build());

        assertThat(actual).isEqualTo(CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOperation(UPLOAD)
                .manageOrdersType(Order.C28_WARRANT_TO_ASSIST)
                .manageOrdersUploadType(Order.C28_WARRANT_TO_ASSIST)
                .build())
            .build());
    }
}
