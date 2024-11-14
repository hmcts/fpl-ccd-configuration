package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.exceptions.NoDocumentException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Component
public class AmendOrderToDownloadPrePopulator implements QuestionBlockOrderPrePopulator {
    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.ORDER_TO_AMEND;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        DocumentReference documentReference = getSelectedOrder(caseData);
        if (documentReference == null) {
            throw new NoDocumentException(getSelectedOrderId(caseData));
        }
        return Map.of("manageOrdersOrderToAmend", documentReference);
    }

    private UUID getSelectedOrderId(CaseData caseData) {
        return caseData.getManageOrdersEventData().getManageOrdersAmendmentList().getValueCodeAsUUID();
    }

    private DocumentReference getSelectedOrder(CaseData caseData) {
        UUID selectedOrderId = getSelectedOrderId(caseData);

        if (StandardDirectionOrder.SDO_COLLECTION_ID.equals(selectedOrderId)) {
            return caseData.getStandardDirectionOrder().getOrderDoc();
        }

        if (UrgentHearingOrder.COLLECTION_ID.equals(selectedOrderId)) {
            return caseData.getUrgentHearingOrder().getOrder();
        }

        Optional<Element<GeneratedOrder>> foundOrder = caseData.getAllOrderCollections()
            .stream()
            .filter(order -> order.getId().equals(selectedOrderId))
            .findFirst();

        if (foundOrder.isPresent()) {
            GeneratedOrder order = foundOrder.get().getValue();
            return (order.isConfidential()) ? order.getDocumentConfidential() : order.getDocument();
        }

        Optional<Element<HearingOrder>> foundCMO = caseData.getSealedCMOs()
            .stream()
            .filter(cmo -> cmo.getId().equals(selectedOrderId))
            .findFirst();

        if (foundCMO.isPresent()) {
            return foundCMO.get().getValue().getOrder();
        }

        throw new NoSuchElementException("Could not find amendable order with id \"" + selectedOrderId + "\"");
    }

}
