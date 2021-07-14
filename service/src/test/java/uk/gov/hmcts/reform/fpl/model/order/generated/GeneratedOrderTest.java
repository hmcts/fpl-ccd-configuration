package uk.gov.hmcts.reform.fpl.model.order.generated;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.GeneratedOrderTypeDescriptor;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.DISCHARGE_OF_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.UPLOAD;
import static uk.gov.hmcts.reform.fpl.model.GeneratedOrderTypeDescriptor.builder;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOther;

class GeneratedOrderTest {

    public static final String AN_ORDER_TYPE = "anOrderType";
    private static final LocalDateTime DATE_TIME_ISSUED = LocalDateTime.of(2020, 9, 21, 4, 2, 3);

    @Test
    void testOrderCanAlwaysBeRemoved() {
        assertThat(GeneratedOrder.builder().build().isRemovable()).isEqualTo(true);
    }

    @Test
    void shouldReturnTypeAppendedByDateOfIssueWhenTitlePresentAndNewType() {
        GeneratedOrder order = GeneratedOrder.builder()
            .type("dancing in september")
            .dateTimeIssued(DATE_TIME_ISSUED)
            .build();

        assertThat(order.asLabel()).isEqualTo("dancing in september - 21 September 2020");
    }

    @Test
    void shouldReturnTitleAppendedByDateOfIssueWhenTitlePresent() {
        GeneratedOrder order = GeneratedOrder.builder()
            .title("do you remember")
            .dateOfIssue("21st September")
            .build();

        assertThat(order.asLabel()).isEqualTo("do you remember - 21st September");
    }

    @Test
    void shouldReturnTypeAppendedByDateOfIssueWhenTitleNotPresent() {
        GeneratedOrder order = GeneratedOrder.builder()
            .type("dancing in september")
            .dateOfIssue("21 September 1978")
            .build();

        assertThat(order.asLabel()).isEqualTo("dancing in september - 21 September 1978");
    }

    @Test
    void testOrderTypesNewFormatFinal() {
        GeneratedOrder order = GeneratedOrder.builder()
            .dateTimeIssued(DATE_TIME_ISSUED)
            .markedFinal(YesNo.YES.getValue())
            .build();

        assertThat(order.isFinalOrder()).isTrue();
    }

    @Test
    void testOrderTypesNewFormatNotFinal() {
        GeneratedOrder order = GeneratedOrder.builder()
            .dateTimeIssued(DATE_TIME_ISSUED)
            .markedFinal(YesNo.NO.getValue())
            .build();

        assertThat(order.isFinalOrder()).isFalse();
    }

    @Test
    void testOrderTypesNewFormatNotFinalUnknown() {
        GeneratedOrder order = GeneratedOrder.builder()
            .dateTimeIssued(DATE_TIME_ISSUED)
            .markedFinal("rubbish")
            .build();

        assertThat(order.isFinalOrder()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("isFinalOrderSource")
    void testOrderTypesThatAreFinalOrder(GeneratedOrderTypeDescriptor orderTypeDescriptor, boolean removable) {
        try (MockedStatic<GeneratedOrderTypeDescriptor> generatedOrderTypeDescriptorMockedStatic =
                 Mockito.mockStatic(GeneratedOrderTypeDescriptor.class)) {

            generatedOrderTypeDescriptorMockedStatic.when(() -> GeneratedOrderTypeDescriptor.fromType(AN_ORDER_TYPE))
                .thenReturn(orderTypeDescriptor);

            assertThat(GeneratedOrder.builder().type(AN_ORDER_TYPE).build().isFinalOrder()).isEqualTo(removable);
        }
    }

    private static Stream<Arguments> isFinalOrderSource() {
        return Stream.of(
            Arguments.of(builder().type(BLANK_ORDER).build(), false),
            Arguments.of(builder().type(CARE_ORDER).subtype(INTERIM).build(), false),
            Arguments.of(builder().type(CARE_ORDER).subtype(FINAL).build(), true),
            Arguments.of(builder().type(SUPERVISION_ORDER).subtype(INTERIM).build(), false),
            Arguments.of(builder().type(SUPERVISION_ORDER).subtype(FINAL).build(), true),
            Arguments.of(builder().type(EMERGENCY_PROTECTION_ORDER).build(), true),
            Arguments.of(builder().type(DISCHARGE_OF_CARE_ORDER).build(), false),
            Arguments.of(builder().type(UPLOAD).build(), false)
        );
    }

    @Test
    void shouldReturnListOfChildrenIdsWhenExisting() {
        UUID childOneId = UUID.randomUUID();
        UUID childTwoId = UUID.randomUUID();

        GeneratedOrder order = GeneratedOrder.builder()
            .children(List.of(
                element(childOneId, Child.builder().build()),
                element(childTwoId, Child.builder().build())
            )).build();

        assertThat(order.getChildrenIDs()).isEqualTo(List.of(childOneId, childTwoId));
    }

    @Test
    void shouldReturnANEmptyListWhenChildrenDoNotExistOnOrder() {
        GeneratedOrder order = GeneratedOrder.builder().build();

        assertThat(order.getChildrenIDs()).isEmpty();
    }

    @Test
    void shouldReturnAmendedOrderType() {
        GeneratedOrder order = GeneratedOrder.builder()
            .type("generated order type")
            .build();

        assertThat(order.getAmendedOrderType()).isEqualTo("generated order type");
    }

    @Test
    void shouldReturnSelectedOthers() {
        List<Element<Other>>  selectedOthers = List.of(element(testOther("Other 1")));
        GeneratedOrder order = GeneratedOrder.builder()
            .type("generated order type")
            .others(selectedOthers)
            .build();

        assertThat(order.getSelectedOthers()).isEqualTo(selectedOthers);
    }

    @Test
    void shouldReturnEmptyListWhenNoSelectedOthers() {
        GeneratedOrder order = GeneratedOrder.builder()
            .type("generated order type")
            .others(emptyList())
            .build();

        assertThat(order.getSelectedOthers()).isEqualTo(emptyList());
    }
}
