package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.stream.Stream;

import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;

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
}
