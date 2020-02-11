package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class PlacementTest {

    @Test
    void shouldReturnEmptyListWhenOnlyPlacementOrderIsPresentInListOfOrderAndNotices() {
        Placement placement = Placement.builder()
                .orderAndNotices(wrapElements(placementOrderAndNoticeOfType(PLACEMENT_ORDER)))
                .build();

        assertThat(unwrapElements(placement.removePlacementOrder().getOrderAndNotices())).isEmpty();
    }

    @Test
    void shouldRemovePlacementOrderFromListOfPlacementOrderAndNoticesWhenPresent() {
        Placement placement = Placement.builder()
                .orderAndNotices(wrapElements(
                        placementOrderAndNoticeOfType(NOTICE_OF_HEARING),
                        placementOrderAndNoticeOfType(PLACEMENT_ORDER)))
                .build();

        assertThat(unwrapElements(placement.removePlacementOrder().getOrderAndNotices()))
                .containsOnly(placementOrderAndNoticeOfType(NOTICE_OF_HEARING));
    }

    private PlacementOrderAndNotices placementOrderAndNoticeOfType(
            PlacementOrderAndNotices.PlacementOrderAndNoticesType noticeOfHearing) {
        return PlacementOrderAndNotices.builder().type(noticeOfHearing).build();
    }
}
