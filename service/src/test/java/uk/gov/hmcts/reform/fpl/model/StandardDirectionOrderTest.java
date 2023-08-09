package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOther;

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

        assertThat(standardDirectionOrder.isRemovable()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenOrderIsNotRemovable() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .orderStatus(DRAFT)
            .build();

        assertThat(standardDirectionOrder.isRemovable()).isFalse();
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

        assertThat(standardDirectionOrder.asLabel()).isEqualTo("Gatekeeping order - 1 January 2020");
    }

    @Test
    void shouldFormatStandardDirectionOrderAsLabelWithProvidedDateOfUpload() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .dateOfUpload(LocalDate.of(2020, 1, 1))
            .build();

        assertThat(standardDirectionOrder.asLabel()).isEqualTo("Gatekeeping order - 1 January 2020");
    }

    @Test
    void shouldFormatStandardDirectionOrderAsLabelWithoutProvidedDateOfIssue() {
        LocalDate now = LocalDate.of(2020, 1, 1);
        try (MockedStatic<LocalDate> date = mockStatic(LocalDate.class)) {
            date.when(LocalDate::now).thenReturn(now);
            StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder().build();
            assertThat(standardDirectionOrder.asLabel()).isEqualTo("Gatekeeping order - 1 January 2020");
        }
    }

    @Test
    void amendableSortDateDateOfUpload() {
        LocalDate date = LocalDate.of(1, 1, 1);
        StandardDirectionOrder sdo = StandardDirectionOrder.builder().dateOfUpload(date).build();
        assertThat(sdo.amendableSortDate()).isEqualTo(date);
    }

    @Test
    void amendableSortDateDateOfIssue() {
        StandardDirectionOrder sdo = StandardDirectionOrder.builder().dateOfIssue("1 January 0001").build();
        assertThat(sdo.amendableSortDate()).isEqualTo(LocalDate.of(1, 1, 1));
    }

    @Test
    void amendableSortDateNull() {
        StandardDirectionOrder sdo = StandardDirectionOrder.builder().build();
        assertThat(sdo.amendableSortDate()).isNull();
    }

    @Test
    void shouldReturnAmendedOrderType() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder().build();
        assertThat(standardDirectionOrder.getModifiedItemType()).isEqualTo("Standard direction order");
    }

    @Test
    void shouldReturnSelectedOthers() {
        List<Element<Other>>  selectedOthers = List.of(element(testOther("Other 1")));
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .others(selectedOthers)
            .build();

        assertThat(standardDirectionOrder.getSelectedOthers()).isEqualTo(selectedOthers);
    }

    @Test
    void shouldReturnEmptyListWhenNoSelectedOthers() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .others(emptyList())
            .build();

        assertThat(standardDirectionOrder.getSelectedOthers()).isEqualTo(emptyList());
    }
}
