package uk.gov.hmcts.reform.fpl.model.order.generated;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.fpl.model.GeneratedOrderTypeDescriptor;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.DISCHARGE_OF_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.UPLOAD;
import static uk.gov.hmcts.reform.fpl.model.GeneratedOrderTypeDescriptor.builder;

class GeneratedOrderTest {

    public static final String AN_ORDER_TYPE = "anOrderType";

    @ParameterizedTest
    @MethodSource("typeToEnumSource")
    void testOrderTypesThatCanBeRemoved(
        GeneratedOrderTypeDescriptor orderTypeDescriptor,
        boolean removable
    ) {
        try (MockedStatic<GeneratedOrderTypeDescriptor> generatedOrderTypeDescriptorMockedStatic =
                 Mockito.mockStatic(GeneratedOrderTypeDescriptor.class)) {

            generatedOrderTypeDescriptorMockedStatic.when(() -> GeneratedOrderTypeDescriptor.fromType(AN_ORDER_TYPE))
                .thenReturn(orderTypeDescriptor);

            assertThat(GeneratedOrder.builder().type(AN_ORDER_TYPE).build().isRemovable()).isEqualTo(removable);
        }
    }

    private static Stream<Arguments> typeToEnumSource() {
        return Stream.of(
            Arguments.of(builder().type(BLANK_ORDER).build(), true),
            Arguments.of(builder().type(CARE_ORDER).subtype(INTERIM).build(),true),
            Arguments.of(builder().type(CARE_ORDER).subtype(FINAL).build(), false),
            Arguments.of(builder().type(SUPERVISION_ORDER).subtype(INTERIM).build(), true),
            Arguments.of(builder().type(SUPERVISION_ORDER).subtype(FINAL).build(), false),
            Arguments.of(builder().type(EMERGENCY_PROTECTION_ORDER).build(), false),
            Arguments.of(builder().type(DISCHARGE_OF_CARE_ORDER).build(), false),
            Arguments.of(builder().type(UPLOAD).build(),false)
        );
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
}
