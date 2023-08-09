package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.UploadedOrderType;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.UPLOAD;

class OrderTypeAndDocumentTest {

    @Nested
    class GetTypeLabel {
        @ParameterizedTest
        @EnumSource(value = GeneratedOrderType.class, names = {"UPLOAD"}, mode = EnumSource.Mode.EXCLUDE)
        void shouldPullLabelOfNonUploadableType(GeneratedOrderType orderType) {
            OrderTypeAndDocument typeAndDocument = OrderTypeAndDocument.builder()
                .type(orderType)
                .build();

            assertThat(typeAndDocument.getTypeLabel()).isEqualTo(orderType.getLabel());
        }

        @Test
        void shouldUseUploadedOrderTypeWhenTypeUploadAndUploadedTypeIsNotOther() {
            UploadedOrderType uploadedOrderType = UploadedOrderType.C30;
            OrderTypeAndDocument typeAndDocument = OrderTypeAndDocument.builder()
                .type(UPLOAD)
                .uploadedOrderType(uploadedOrderType)
                .build();

            assertThat(typeAndDocument.getTypeLabel()).isEqualTo(uploadedOrderType.getFullLabel());
        }

        @Test
        void shouldUseOrderNameWhenTypeIsUploadAndUploadedTypeIsOther() {
            String orderName = "some order";
            OrderTypeAndDocument typeAndDocument = OrderTypeAndDocument.builder()
                .type(UPLOAD)
                .uploadedOrderType(UploadedOrderType.OTHER)
                .orderName(orderName)
                .build();

            assertThat(typeAndDocument.getTypeLabel()).isEqualTo(orderName);
        }
    }

    @Nested
    class IsCloseableOrder {

        @ParameterizedTest
        @EnumSource(value = GeneratedOrderType.class, names = {"BLANK_ORDER"})
        void shouldReturnFalse(GeneratedOrderType orderType) {
            OrderTypeAndDocument typeAndDoc = OrderTypeAndDocument.builder().type(orderType).build();

            assertThat(typeAndDoc.isClosable()).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = GeneratedOrderType.class, names = {"EMERGENCY_PROTECTION_ORDER", "DISCHARGE_OF_CARE_ORDER"})
        void shouldReturnTrue(GeneratedOrderType orderType) {
            OrderTypeAndDocument typeAndDoc = OrderTypeAndDocument.builder().type(orderType).build();

            assertThat(typeAndDoc.isClosable()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = GeneratedOrderType.class, names = {"CARE_ORDER", "SUPERVISION_ORDER"})
        void shouldReturnTrueWhenOrderIsFinal(GeneratedOrderType orderType) {
            OrderTypeAndDocument typeAndDoc = OrderTypeAndDocument.builder().type(orderType).subtype(FINAL).build();

            assertThat(typeAndDoc.isClosable()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = GeneratedOrderType.class, names = {"CARE_ORDER", "SUPERVISION_ORDER"})
        void shouldReturnFalseWhenOrderIsInterim(GeneratedOrderType orderType) {
            OrderTypeAndDocument typeAndDoc = OrderTypeAndDocument.builder().type(orderType).subtype(INTERIM).build();

            assertThat(typeAndDoc.isClosable()).isFalse();
        }
    }
}
