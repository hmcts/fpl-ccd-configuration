package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.ActionType;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;

@ExtendWith(SpringExtension.class)
class CaseManagementOrderTest {

    @Test
    void shouldReturnFalseWhenActionTypeEqualsSendToAllParties() {
        assertFalse(order(SEND_TO_ALL_PARTIES).isDraft());
    }

    @EnumSource(value = ActionType.class, names = {"JUDGE_REQUESTED_CHANGE", "SELF_REVIEW"})
    @ParameterizedTest
    void shouldReturnTrueWhenActionTypeEqualsOtherThanSendToAllParties(ActionType type) {
        assertTrue(order(type).isDraft());
    }

    @Test
    void shouldReturnTrueWhenActionTypeIsNull() {
        assertTrue(CaseManagementOrder.builder().build().isDraft());
    }

    private CaseManagementOrder order(ActionType type) {
        return CaseManagementOrder.builder()
            .action(OrderAction.builder()
                .type(type)
                .build())
            .build();
    }
}
