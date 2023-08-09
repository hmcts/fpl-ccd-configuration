package uk.gov.hmcts.reform.fpl.service.summary;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary.emptySummary;

class CaseSummaryOrdersRequestedGeneratorTest {

    CaseSummaryOrdersRequestedGenerator underTest = new CaseSummaryOrdersRequestedGenerator();

    @Test
    void testIfNoOrderNeeded() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder().build());

        assertThat(actual).isEqualTo(emptySummary());
    }

    @Test
    void testIfOrderWithNoList() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder().orders(Orders.builder().build()).build());

        assertThat(actual).isEqualTo(emptySummary());
    }

    @Test
    void testIfOrderWithNoListOfTypes() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .orders(Orders.builder().orderType(emptyList()).build())
            .build());

        assertThat(actual).isEqualTo(emptySummary());
    }

    @Test
    void testIfOrderWithSingleOrder() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .orders(Orders.builder().orderType(List.of(
                OrderType.CARE_ORDER
            )).build())
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryOrdersRequested("Care order")
            .build());
    }

    @Test
    void testIfOrderWithMultipleOrders() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .orders(Orders.builder().orderType(List.of(
                OrderType.CARE_ORDER,
                OrderType.SUPERVISION_ORDER
            )).build())
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryOrdersRequested("Care order, Supervision order")
            .build());
    }
}
