package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOther;

class HearingOrderTest {
    @Test
    void shouldReturnAmendedOrderType() {
        HearingOrder hearingOrder = HearingOrder.builder().build();
        assertThat(hearingOrder.getModifiedItemType()).isEqualTo("Case management order");
    }

    @Test
    void shouldReturnSelectedOthers() {
        List<Element<Other>>  selectedOthers = List.of(element(testOther("Other 1")));
        HearingOrder hearingOrder = HearingOrder.builder()
            .others(selectedOthers)
            .build();

        assertThat(hearingOrder.getSelectedOthers()).isEqualTo(selectedOthers);
    }

    @Test
    void shouldReturnEmptyListWhenNoSelectedOthers() {
        HearingOrder hearingOrder = HearingOrder.builder()
            .others(emptyList())
            .build();

        assertThat(hearingOrder.getSelectedOthers()).isEqualTo(emptyList());
    }
}
