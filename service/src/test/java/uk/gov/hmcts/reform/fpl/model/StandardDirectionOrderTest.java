package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
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
    void shouldReturnTrueWhenOrderIsRemovable() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .orderStatus(SEALED)
            .build();

        assertThat(standardDirectionOrder.isSealed()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenOrderIsNotRemovable() {
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

    @Test
    void shouldReturnFixedUUID() {
        UUID expectedId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder().build();

        assertThat(standardDirectionOrder.getCollectionId()).isEqualTo(expectedId);
    }

    @Test
    void shouldFormatStandardDirectionOrderAsLabelWithProvidedDateOfIssue() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .dateOfIssue("1 January 2020")
            .build();

        assertThat(standardDirectionOrder.asLabel()).isEqualTo(
            String.format("Gatekeeping order - %s", "1 January 2020"));
    }

    @Test
    void shouldFormatStandardDirectionOrderAsLabelWithoutProvidedDateOfIssue() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder().build();

        assertThat(standardDirectionOrder.asLabel()).isEqualTo(
            String.format("Gatekeeping order - %s", formatLocalDateToString(LocalDate.now(), "d MMMM yyyy")));
    }
}
