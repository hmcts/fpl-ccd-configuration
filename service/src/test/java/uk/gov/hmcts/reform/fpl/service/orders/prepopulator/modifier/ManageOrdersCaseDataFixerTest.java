package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.modifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderOperation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    @Test
    void shouldNotModifyAmendmentListIfAmendJourney() {
        UUID orderID = UUID.randomUUID();
        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.builder().code(orderID).build())
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("manageOrdersAmendmentList", expectedList);
        data.put("manageOrdersOperation", AMEND);

        CaseDetails details = CaseDetails.builder().data(data).build();

        CaseDetails actual = underTest.fixAndRetriveCaseDetails(details);

        assertThat(actual.getData().get("manageOrdersAmendmentList")).isEqualTo(expectedList);
    }

    @ParameterizedTest
    @EnumSource(value = OrderOperation.class, names = {"CREATE", "UPLOAD"})
    void shouldRemoveAmendmentListIfNotAmendJourney(OrderOperation operation) {
        UUID orderID = UUID.randomUUID();
        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.builder().code(orderID).build())
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("manageOrdersAmendmentList", expectedList);
        data.put("manageOrdersOperation", operation);

        CaseDetails details = CaseDetails.builder().data(data).build();

        CaseDetails actual = underTest.fixAndRetriveCaseDetails(details);

        assertThat(actual.getData()).isEqualTo(Map.of("manageOrdersOperation", operation));
    }
}
