package uk.gov.hmcts.reform.fpl.model.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GatekeepingOrderEventDataTest {

    @Test
    void temporaryFields() {
        assertThat(GatekeepingOrderEventData.temporaryFields()).containsExactly(
            "urgentHearingOrderDocument", "urgentHearingAllocation", "showUrgentHearingAllocation"
        );
    }
}
