package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

class GatekeepingOrderSealDecisionTest {

    @Test
    void shouldReturnTrueIfOrderStatusIsSealed() {
        final GatekeepingOrderSealDecision decision = GatekeepingOrderSealDecision.builder()
            .orderStatus(OrderStatus.SEALED)
            .build();

        assertThat(decision.isSealed()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, mode = EXCLUDE, names = {"SEALED"})
    void shouldReturnFalseIfOrderStatusIsNotSealed(OrderStatus status) {
        final GatekeepingOrderSealDecision decision = GatekeepingOrderSealDecision.builder()
            .orderStatus(status)
            .build();

        assertThat(decision.isSealed()).isFalse();
    }
}
