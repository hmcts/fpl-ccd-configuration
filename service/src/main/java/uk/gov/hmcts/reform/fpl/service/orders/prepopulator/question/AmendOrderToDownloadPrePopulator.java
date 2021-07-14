package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.springframework.stereotype.Component;
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
        return Map.of("manageOrdersOrderToAmend", getSelectedOrder(caseData));
    }

    private DocumentReference getSelectedOrder(CaseData caseData) {
        UUID selectedOrderId = caseData.getManageOrdersEventData().getManageOrdersAmendmentList().getValueCodeAsUUID();

        if (StandardDirectionOrder.COLLECTION_ID.equals(selectedOrderId)) {
            return caseData.getStandardDirectionOrder().getOrderDoc();
        }

        if (UrgentHearingOrder.COLLECTION_ID.equals(selectedOrderId)) {
            return caseData.getUrgentHearingOrder().getOrder();
        }

        Optional<Element<GeneratedOrder>> foundOrder = caseData.getOrderCollection()
            .stream()
            .filter(order -> order.getId().equals(selectedOrderId))
            .findFirst();

        if (foundOrder.isPresent()) {
            return foundOrder.get().getValue().getDocument();
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
