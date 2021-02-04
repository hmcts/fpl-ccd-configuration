package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.exceptions.removeorder.RemovableOrderActionNotFoundException;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;

@ExtendWith(MockitoExtension.class)
class OrderRemovalActionsTest {
    @Mock
    private SealedCMORemovalAction sealedCMORemovalAction;

    @Mock
    private GeneratedOrderRemovalAction generatedOrderRemovalAction;

    @Mock
    private SDORemovalAction sdoRemovalAction;

    @Mock
    private DraftCMORemovalAction draftCMORemovalAction;

    @InjectMocks
    private OrderRemovalActions orderRemovalActions;

    private static final HearingOrder SEALED_CASE_MANAGEMENT_ORDER = HearingOrder.builder().status(APPROVED).build();
    private static final HearingOrder DRAFT_CASE_MANAGEMENT_ORDER = HearingOrder.builder().status(DRAFT).build();
    private static final GeneratedOrder GENERATED_ORDER = GeneratedOrder.builder().build();
    private static final StandardDirectionOrder STANDARD_DIRECTION_ORDER = StandardDirectionOrder.builder().build();

    @Test
    void shouldReturnSealedOrderActionWhenGettingActionForGeneratedOrder() {
        when(sealedCMORemovalAction.isAccepted(SEALED_CASE_MANAGEMENT_ORDER)).thenReturn(true);

        assertThat(orderRemovalActions.getAction(SEALED_CASE_MANAGEMENT_ORDER))
            .isEqualTo(sealedCMORemovalAction);
    }

    @Test
    void shouldReturnDraftOrderActionWhenGettingActionForGeneratedOrder() {
        when(draftCMORemovalAction.isAccepted(DRAFT_CASE_MANAGEMENT_ORDER)).thenReturn(true);

        assertThat(orderRemovalActions.getAction(DRAFT_CASE_MANAGEMENT_ORDER))
            .isEqualTo(draftCMORemovalAction);
    }

    @Test
    void shouldReturnCMOActionWhenGettingActionForCMO() {
        when(generatedOrderRemovalAction.isAccepted(GENERATED_ORDER)).thenReturn(true);

        assertThat(orderRemovalActions.getAction(GENERATED_ORDER))
            .isEqualTo(generatedOrderRemovalAction);
    }

    @Test
    void shouldReturnSDOActionWhenGettingActionForSDO() {
        when(sdoRemovalAction.isAccepted(STANDARD_DIRECTION_ORDER)).thenReturn(true);

        assertThat(orderRemovalActions.getAction(STANDARD_DIRECTION_ORDER))
            .isEqualTo(sdoRemovalAction);
    }

    @Test
    void shouldThrowAnExceptionWhenActionTypeFailsToMatchToRemovableOrder() {
        when(sealedCMORemovalAction.isAccepted(GENERATED_ORDER)).thenReturn(false);

        assertThatThrownBy(() -> orderRemovalActions.getAction(GENERATED_ORDER))
            .isInstanceOf(RemovableOrderActionNotFoundException.class)
            .hasMessage(format("Removable order action for order of type %s not found", GENERATED_ORDER.getClass()
                .getSimpleName()));
    }
}
