package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_PREVENT_REMOVAL;

@Component
public class EPOAddressPrePopulator implements QuestionBlockOrderPrePopulator {

    private static final String CASE_FIELD_KEY = "manageOrdersEpoRemovalAddress";

    @Override
    public OrderQuestionBlock accept() {
        return EPO_PREVENT_REMOVAL;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        if (caseData.getOrders() != null && caseData.getOrders().getAddress() != null) {
            return Map.of(CASE_FIELD_KEY, caseData.getOrders().getAddress());
        }
        return Map.of();
    }
}
