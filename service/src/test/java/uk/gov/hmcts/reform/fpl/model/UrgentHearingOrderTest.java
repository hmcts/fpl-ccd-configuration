package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOther;

class UrgentHearingOrderTest {
    @Test
    void shouldReturnAmendedOrderType() {
        UrgentHearingOrder urgentHearingOrder = UrgentHearingOrder.builder().build();
        assertThat(urgentHearingOrder.getModifiedItemType()).isEqualTo("Urgent hearing order");
    }

    @Test
    void shouldReturnSelectedOthers() {
        List<Element<Other>>  selectedOthers = List.of(element(testOther("Other 1")));
        UrgentHearingOrder urgentHearingOrder = UrgentHearingOrder.builder()
            .others(selectedOthers)
            .build();

        assertThat(urgentHearingOrder.getSelectedOthers()).isEqualTo(selectedOthers);
    }

    @Test
    void shouldReturnEmptyListWhenNoSelectedOthers() {
        UrgentHearingOrder urgentHearingOrder = UrgentHearingOrder.builder()
            .others(emptyList())
            .build();

        assertThat(urgentHearingOrder.getSelectedOthers()).isEqualTo(emptyList());
    }
}
