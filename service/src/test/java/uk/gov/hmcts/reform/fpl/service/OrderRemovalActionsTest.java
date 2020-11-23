package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.service.removeorder.CMOOrderRemovalAction;
import uk.gov.hmcts.reform.fpl.service.removeorder.OrderRemovalActions;
import uk.gov.hmcts.reform.fpl.service.removeorder.OtherOrderRemovalAction;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OrderRemovalActionsTest {

    @Test
    void testGetActions() {
        CMOOrderRemovalAction cmoOrderRemovalAction = mock(CMOOrderRemovalAction.class);
        OtherOrderRemovalAction otherOrderRemovalAction = mock(OtherOrderRemovalAction.class);
        assertThat(new OrderRemovalActions(
            cmoOrderRemovalAction,
            otherOrderRemovalAction
        ).getActions()).isEqualTo(List.of(
            cmoOrderRemovalAction,
            otherOrderRemovalAction
        ));
    }
}
