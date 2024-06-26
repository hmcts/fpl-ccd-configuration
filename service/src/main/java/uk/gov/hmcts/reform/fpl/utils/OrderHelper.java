package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

public class OrderHelper {

    private OrderHelper() {
    }

    public static String getFullOrderType(GeneratedOrderType type) {
        return getFullOrderType(type, null);
    }

    public static String getFullOrderType(GeneratedOrderType type, GeneratedOrderSubtype subtype) {
        if (subtype == null) {
            return type.getLabel();
        } else {
            return String.format("%s %s", subtype.getLabel(), type.getLabel().toLowerCase());
        }
    }

    public static String getFullOrderType(OrderTypeAndDocument orderTypeAndDocument) {
        return getFullOrderType(orderTypeAndDocument.getType(), orderTypeAndDocument.getSubtype());
    }

    public static boolean isOfType(GeneratedOrder order, GeneratedOrderType orderType) {
        return Stream.of(FINAL, INTERIM, null)
            .anyMatch(subtype -> getFullOrderType(orderType, subtype).equals(order.getType()));
    }

    public static Optional<LocalDate> getLatestApprovalDateOfFinalOrders(CaseData caseData) {
        return unwrapElements(Optional.ofNullable(caseData.getOrderCollection()).orElse(List.of())).stream()
            .filter(GeneratedOrder::isFinalOrder)
            .filter(order -> order.getApprovalDate() != null || order.getApprovalDateTime() != null)
            .map(order -> {
                if (isEmpty(order.getApprovalDateTime())) {
                    return order.getApprovalDate();
                } else {
                    LocalDate convertedApprovalDateTime = order.getApprovalDateTime().toLocalDate();
                    if (isEmpty(order.getApprovalDate())) {
                        return convertedApprovalDateTime;
                    } else {
                        return (convertedApprovalDateTime.isAfter(order.getApprovalDate()))
                            ? convertedApprovalDateTime : order.getApprovalDate();
                    }
                }
            })
            .max(Comparator.naturalOrder());
    }
}
