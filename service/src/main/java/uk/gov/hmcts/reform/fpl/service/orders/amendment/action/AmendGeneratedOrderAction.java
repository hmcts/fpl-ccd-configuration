package uk.gov.hmcts.reform.fpl.service.orders.amendment.action;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AmendGeneratedOrderAction implements AmendOrderAction {
    private static final String CASE_FIELD = "orderCollection";

    private final Time time;

    @Override
    public boolean accept(CaseData caseData) {
        UUID selectedOrderId = caseData.getManageOrdersEventData().getManageOrdersAmendmentList().getValueCodeAsUUID();
        return caseData.getAllOrderCollections().stream()
            .anyMatch(order -> Objects.equals(selectedOrderId, order.getId()));
    }

    @Override
    public Map<String, Object> applyAmendedOrder(CaseData caseData, DocumentReference amendedDocument,
                                                 List<Element<Other>> selectedOthers) {
        UUID selectedOrderId = caseData.getManageOrdersEventData().getManageOrdersAmendmentList().getValueCodeAsUUID();
        List<Element<GeneratedOrder>> orders = caseData.getOrderCollection();

        boolean notConfidential = ElementUtils.findElement(selectedOrderId, orders).isPresent();

        if (notConfidential) {
            return updateOrdersAndCaseField(orders, CASE_FIELD, selectedOrderId, amendedDocument, selectedOthers);
        } else {
            Map<String, Object> updates = new HashMap<>();
            caseData.getConfidentialOrders().processAllConfidentialOrders((suffix, existingConfidentialOrders) -> {
                if (ElementUtils.findElement(selectedOrderId, existingConfidentialOrders).isPresent()) {
                    updates.putAll(updateOrdersAndCaseField(existingConfidentialOrders,
                        caseData.getConfidentialOrders().getFieldBaseName() + suffix,
                        selectedOrderId, amendedDocument, selectedOthers));
                }
            });
            return updates;
        }
    }

    private Map<String, Object> updateOrdersAndCaseField(List<Element<GeneratedOrder>> existingOrders,
                                                         String caseField,
                                                         UUID selectedOrderId,
                                                         DocumentReference amendedDocument,
                                                         List<Element<Other>> selectedOthers) {
        existingOrders.stream()
            .filter(order -> Objects.equals(order.getId(), selectedOrderId))
            .findFirst()
            .ifPresent(order -> {
                GeneratedOrder.GeneratedOrderBuilder builder = order.getValue().toBuilder()
                    .amendedDate(time.now().toLocalDate())
                    .others(selectedOthers);

                if (isNotEmpty(order.getValue().getDocumentConfidential())) {
                    builder = builder.documentConfidential(amendedDocument);
                } else {
                    builder = builder.document(amendedDocument);
                }

                existingOrders.set(existingOrders.indexOf(order), element(order.getId(), builder.build()));
            });

        return Map.of(caseField, existingOrders);
    }
}
