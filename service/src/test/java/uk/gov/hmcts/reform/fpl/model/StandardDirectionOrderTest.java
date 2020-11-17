package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class StandardDirectionOrderTest {
    @Test
    void shouldReturnTrueWhenOrderIsSealed() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .orderStatus(SEALED)
            .build();

        assertThat(standardDirectionOrder.isSealed()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenOrderIsInDraft() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .orderStatus(DRAFT)
            .build();

        assertThat(standardDirectionOrder.isSealed()).isFalse();
    }

    @Test
    void shouldSetDirectionsToAnEmptyList() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .directions(List.of(element(Direction.builder()
                .readOnly("Some direction")
                .build())))
            .build();

        standardDirectionOrder.setDirectionsToEmptyList();

        assertThat(standardDirectionOrder.getDirections()).isEmpty();
    }
}
