package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOther;

class UrgentHearingOrderTest {
    @Test
    void shouldReturnAmendedOrderType() {
        UrgentHearingOrder urgentHearingOrder = UrgentHearingOrder.builder().build();
        assertThat(urgentHearingOrder.getAmendedOrderType()).isEqualTo("urgent hearing order");
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
