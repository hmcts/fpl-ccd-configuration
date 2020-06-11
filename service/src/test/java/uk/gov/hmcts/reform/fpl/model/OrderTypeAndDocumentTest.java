package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;

class OrderTypeAndDocumentTest {

    @Nested
    class IsCloseableOrder {

        @ParameterizedTest
        @EnumSource(value = GeneratedOrderType.class, names = {"BLANK_ORDER"})
        void shouldReturnFalse(GeneratedOrderType orderType) {
            OrderTypeAndDocument typeAndDoc = OrderTypeAndDocument.builder().type(orderType).build();

            assertThat(typeAndDoc.isClosable()).isEqualTo(false);
        }

        @ParameterizedTest
        @EnumSource(value = GeneratedOrderType.class, names = {"EMERGENCY_PROTECTION_ORDER", "DISCHARGE_OF_CARE_ORDER"})
        void shouldReturnTrue(GeneratedOrderType orderType) {
            OrderTypeAndDocument typeAndDoc = OrderTypeAndDocument.builder().type(orderType).build();

            assertThat(typeAndDoc.isClosable()).isEqualTo(true);
        }

        @ParameterizedTest
        @EnumSource(value = GeneratedOrderType.class, names = {"CARE_ORDER", "SUPERVISION_ORDER"})
        void shouldReturnTrueWhenOrderIsFinal(GeneratedOrderType orderType) {
            OrderTypeAndDocument typeAndDoc = OrderTypeAndDocument.builder().type(orderType).subtype(FINAL).build();

            assertThat(typeAndDoc.isClosable()).isEqualTo(true);
        }

        @ParameterizedTest
        @EnumSource(value = GeneratedOrderType.class, names = {"CARE_ORDER", "SUPERVISION_ORDER"})
        void shouldReturnFalseWhenOrderIsInterim(GeneratedOrderType orderType) {
            OrderTypeAndDocument typeAndDoc = OrderTypeAndDocument.builder().type(orderType).subtype(INTERIM).build();

            assertThat(typeAndDoc.isClosable()).isEqualTo(false);
        }
    }

    private static Stream<Arguments> typeSource() {
        return Stream.of(
            Arguments.of(BLANK_ORDER, "Blank order (C21)"),
            Arguments.of(CARE_ORDER, "Care order"),
            Arguments.of(SUPERVISION_ORDER, "Supervision order")
        );
    }
}
