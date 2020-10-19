package uk.gov.hmcts.reform.fpl.model.order.generated;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.fpl.model.GeneratedOrderTypeDescriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeneratedOrderTest {

    public static final String AN_ORDER_TYPE = "anOrderType";
    public static final boolean IS_REMOVABLE = true;

    private final GeneratedOrderTypeDescriptor generatedOrderTypeDescriptor = mock(GeneratedOrderTypeDescriptor.class);

    @Test
    void testIsRemovable() {
        try (MockedStatic<GeneratedOrderTypeDescriptor> orderTypeDescriptor =
                 Mockito.mockStatic(GeneratedOrderTypeDescriptor.class)) {

            orderTypeDescriptor.when(() -> GeneratedOrderTypeDescriptor.fromType(AN_ORDER_TYPE))
                .thenReturn(generatedOrderTypeDescriptor);
            when(generatedOrderTypeDescriptor.isRemovable()).thenReturn(IS_REMOVABLE);

            assertThat(GeneratedOrder.builder().type(AN_ORDER_TYPE).build().isRemovable()).isEqualTo(IS_REMOVABLE);
        }
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
