package uk.gov.hmcts.reform.fpl.service.orders.history;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SealedOrderHistoryFinalMarkerTest {

    private final SealedOrderHistoryFinalMarker underTest = new SealedOrderHistoryFinalMarker();

    @Test
    void testNonFinalOrder() {
        YesNo actual = underTest.calculate(CaseData.builder().manageOrdersEventData(ManageOrdersEventData.builder()
            .manageOrdersType(Order.C21_BLANK_ORDER)
            .build()).build());

        assertThat(actual).isEqualTo(YesNo.NO);
    }

    @Test
    void testFinalOrder() {
        YesNo actual = underTest.calculate(CaseData.builder().manageOrdersEventData(ManageOrdersEventData.builder()
            .manageOrdersType(Order.C32_CARE_ORDER)
            .build()).build());

        assertThat(actual).isEqualTo(YesNo.YES);
    }

    @Test
    void testAskedFinalOrderAnswerYes() {
        YesNo actual = underTest.calculate(CaseData.builder().manageOrdersEventData(ManageOrdersEventData.builder()
            .manageOrdersType(Order.C32B_DISCHARGE_OF_CARE_ORDER)
            .manageOrdersIsFinalOrder(YesNo.YES.getValue())
            .build()).build());

        assertThat(actual).isEqualTo(YesNo.YES);
    }

    @Test
    void testAskedFinalOrderAnswerNo() {
        YesNo actual = underTest.calculate(CaseData.builder().manageOrdersEventData(ManageOrdersEventData.builder()
            .manageOrdersType(Order.C32B_DISCHARGE_OF_CARE_ORDER)
            .manageOrdersIsFinalOrder(YesNo.NO.getValue())
            .build()).build());

        assertThat(actual).isEqualTo(YesNo.NO);
    }
}
