package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderRemovalActionsTest {
    @Mock
    private CMOOrderRemovalAction cmoOrderRemovalAction;

    @Mock
    private GeneratedOrderRemovalAction generatedOrderRemovalAction;

    @InjectMocks
    private OrderRemovalActions orderRemovalActions;

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final CaseManagementOrder CASE_MANAGEMENT_ORDER = CaseManagementOrder.builder().build();
    private static final GeneratedOrder GENERATED_ORDER = GeneratedOrder.builder().build();

    @Test
    void shouldReturnGeneratedOrderActionWhenGettingActionForGeneratedOrder() {
        when(cmoOrderRemovalAction.isAccepted(CASE_MANAGEMENT_ORDER)).thenReturn(true);

        assertThat(orderRemovalActions.getAction(ORDER_ID, CASE_MANAGEMENT_ORDER))
            .isEqualTo(cmoOrderRemovalAction);
    }

    @Test
    void shouldReturnCMOActionWhenGettingActionForCMO() {
        when(generatedOrderRemovalAction.isAccepted(GENERATED_ORDER)).thenReturn(true);

        assertThat(orderRemovalActions.getAction(ORDER_ID, GENERATED_ORDER))
            .isEqualTo(generatedOrderRemovalAction);
    }

    @Test
    void shouldThrowAnExceptionWhenActionTypeFailsToMatchToRemovableOrder() {
        when(cmoOrderRemovalAction.isAccepted(GENERATED_ORDER)).thenReturn(false);

        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class,
            () -> orderRemovalActions.getAction(ORDER_ID, GENERATED_ORDER)
        );

        assertThat(actualException.getMessage()).isEqualTo(
            format("Action not found for order %s", ORDER_ID)
        );
    }
}
