package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;

class OrderHelperTest {

    @Nested
    class FullOrderType {
        @Test
        void shouldGetFullOrderTypeForFinalOrder() {
            assertThat(OrderHelper.getFullOrderType(CARE_ORDER, FINAL)).isEqualTo("Final care order");
        }

        @Test
        void shouldGetFullOrderTypeForInterimOrder() {
            assertThat(OrderHelper.getFullOrderType(CARE_ORDER, INTERIM)).isEqualTo("Interim care order");
        }

        @Test
        void shouldGetFullOrderTypeForOrderWithoutSubtype() {
            assertThat(OrderHelper.getFullOrderType(CARE_ORDER, null)).isEqualTo("Care order");
            assertThat(OrderHelper.getFullOrderType(CARE_ORDER)).isEqualTo("Care order");
        }
    }

    @Nested
    class FullOrderTypeForOrderTypeAndDocument {
        @Test
        void shouldGetFullOrderTypeForFinalOrder() {
            OrderTypeAndDocument orderTypeAndDocument = OrderTypeAndDocument.builder()
                .type(SUPERVISION_ORDER)
                .subtype(FINAL)
                .build();
            assertThat(OrderHelper.getFullOrderType(orderTypeAndDocument)).isEqualTo("Final supervision order");
        }

        @Test
        void shouldGetFullOrderTypeForInterimOrder() {
            OrderTypeAndDocument orderTypeAndDocument = OrderTypeAndDocument.builder()
                .type(SUPERVISION_ORDER)
                .subtype(INTERIM)
                .build();
            assertThat(OrderHelper.getFullOrderType(orderTypeAndDocument)).isEqualTo("Interim supervision order");
        }

        @Test
        void shouldGetFullOrderTypeForOrderWithoutSubtype() {
            OrderTypeAndDocument orderTypeAndDocument = OrderTypeAndDocument.builder()
                .type(SUPERVISION_ORDER)
                .build();
            assertThat(OrderHelper.getFullOrderType(orderTypeAndDocument)).isEqualTo("Supervision order");
        }
    }

    @Nested
    class GeneratedOrderType {

        @ParameterizedTest
        @ValueSource(strings = {"Final care order", "Interim care order", "Care order"})
        void shouldReturnFalseWhenOrderIsOfRequestedType(String fullType) {
            GeneratedOrder order = GeneratedOrder.builder()
                .type(fullType)
                .build();

            assertThat(OrderHelper.isOfType(order, CARE_ORDER)).isTrue();
        }


        @ParameterizedTest
        @ValueSource(strings = {"Final care order", "Interim care order", "Care order"})
        void shouldReturnFalseWhenOrderIsNotOfRequestedType(String fullType) {
            GeneratedOrder order = GeneratedOrder.builder()
                .type(fullType)
                .build();

            assertThat(OrderHelper.isOfType(order, SUPERVISION_ORDER)).isFalse();
        }

    }
}
