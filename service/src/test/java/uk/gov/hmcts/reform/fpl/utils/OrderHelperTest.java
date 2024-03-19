package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

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

    @Nested
    class GetLatestApprovalDateOfFinalOrders {
        private final LocalDateTime todayDateTime = LocalDateTime.now();
        private final LocalDateTime latestApprovalDateTime = todayDateTime.minusDays(1);
        private final LocalDate latestApprovalDate = latestApprovalDateTime.toLocalDate();
        private final Element<GeneratedOrder> latestFinalOrder = element(GeneratedOrder.builder()
            .dateTimeIssued(todayDateTime)
            .approvalDate(latestApprovalDate).build());
        private final Element<GeneratedOrder> latestFinalOrderWithApprovalDateTime = element(GeneratedOrder.builder()
            .dateTimeIssued(todayDateTime).markedFinal(YesNo.YES.getValue())
            .approvalDateTime(latestApprovalDateTime).build());
        private final Element<GeneratedOrder> finalOrderWithApprovalDate = element(GeneratedOrder.builder()
            .dateTimeIssued(todayDateTime).markedFinal(YesNo.YES.getValue())
            .approvalDate(latestApprovalDate.minusDays(1)).build());
        private final Element<GeneratedOrder> finalOrderWithApprovalDateTime = element(GeneratedOrder.builder()
            .dateTimeIssued(todayDateTime).markedFinal(YesNo.YES.getValue())
            .approvalDate(latestApprovalDate.minusDays(2)).build());
        private final Element<GeneratedOrder> orderWithApprovalDateTime = element(GeneratedOrder.builder()
            .dateTimeIssued(todayDateTime).markedFinal(YesNo.NO.getValue())
            .approvalDate(latestApprovalDate.minusDays(3)).build());
        private final Element<GeneratedOrder> orderWithApprovalDate = element(GeneratedOrder.builder()
            .dateTimeIssued(todayDateTime)
            .approvalDate(latestApprovalDate.minusDays(4)).build());

        @Test
        void shouldReturnLatestApprovalDate() {
            CaseData caseData = CaseData.builder()
                .orderCollection(List.of(
                    latestFinalOrder, latestFinalOrderWithApprovalDateTime, finalOrderWithApprovalDate,
                    finalOrderWithApprovalDateTime, orderWithApprovalDate, orderWithApprovalDateTime))
                .build();

            assertThat(OrderHelper.getLatestApprovalDateOfFinalOrders(caseData))
                .isEqualTo(Optional.of(latestApprovalDate));
        }

        @Test
        void shouldReturnEmptyOptionalWhenNoFinalOrderFound() {
            CaseData caseData = CaseData.builder()
                .orderCollection(List.of(orderWithApprovalDate, orderWithApprovalDateTime))
                .build();

            assertThat(OrderHelper.getLatestApprovalDateOfFinalOrders(caseData)).isEqualTo(Optional.empty());
        }

        @Test
        void shouldReturnEmptyOptionalWhenNull() {
            CaseData caseData = CaseData.builder()
                .orderCollection(null)
                .build();

            assertThat(OrderHelper.getLatestApprovalDateOfFinalOrders(caseData)).isEqualTo(Optional.empty());
        }
    }
}
