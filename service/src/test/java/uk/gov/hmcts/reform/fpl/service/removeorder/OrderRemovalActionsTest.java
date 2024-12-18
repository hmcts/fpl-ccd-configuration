package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.exceptions.removaltool.RemovableOrderActionNotFoundException;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;

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

    @Mock
    private DraftOrderRemovalAction draftOrderRemovalAction;

    @Mock
    private RefusedHearingOrderRemovalAction refusedHearingOrderRemovalAction;

    @InjectMocks
    private OrderRemovalActions orderRemovalActions;

    private static final HearingOrder SEALED_CASE_MANAGEMENT_ORDER = hearingOrder(AGREED_CMO, APPROVED);
    private static final HearingOrder DRAFT_CASE_MANAGEMENT_ORDER = hearingOrder(DRAFT_CMO, DRAFT);
    private static final HearingOrder DRAFT_ORDER = hearingOrder(C21, SEND_TO_JUDGE);
    private static final HearingOrder RETURNED_ORDER = hearingOrder(C21, RETURNED);
    private static final GeneratedOrder GENERATED_ORDER = GeneratedOrder.builder().build();
    private static final StandardDirectionOrder STANDARD_DIRECTION_ORDER = StandardDirectionOrder.builder().build();

    @Test
    void shouldReturnSealedOrderActionWhenGettingActionForSealedCaseManagementOrder() {
        when(sealedCMORemovalAction.isAccepted(SEALED_CASE_MANAGEMENT_ORDER)).thenReturn(true);

        assertThat(orderRemovalActions.getAction(SEALED_CASE_MANAGEMENT_ORDER))
            .isEqualTo(sealedCMORemovalAction);
    }

    @Test
    void shouldReturnDraftCMOOrderActionWhenGettingActionForDraftCaseManagementOrder() {
        when(draftCMORemovalAction.isAccepted(DRAFT_CASE_MANAGEMENT_ORDER)).thenReturn(true);

        assertThat(orderRemovalActions.getAction(DRAFT_CASE_MANAGEMENT_ORDER))
            .isEqualTo(draftCMORemovalAction);
    }

    @Test
    void shouldReturnDraftOrderActionWhenGettingActionForDraftOrder() {
        when(draftOrderRemovalAction.isAccepted(DRAFT_ORDER)).thenReturn(true);

        assertThat(orderRemovalActions.getAction(DRAFT_ORDER)).isEqualTo(draftOrderRemovalAction);
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
    void shouldReturnRefusedHearingOrderActionWhenGettingActionForRefusedOrder() {
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

    private static HearingOrder hearingOrder(HearingOrderType hearingOrderType, CMOStatus status) {
        return HearingOrder.builder().type(hearingOrderType).status(status).build();
    }

}
