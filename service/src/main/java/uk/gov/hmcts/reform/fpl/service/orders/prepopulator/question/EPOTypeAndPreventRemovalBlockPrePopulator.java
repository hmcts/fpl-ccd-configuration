package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_TYPE_AND_PREVENT_REMOVAL;

@Component
public class EPOTypeAndPreventRemovalBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    private static final String REMOVAL_ADDRESS_FIELD = "manageOrdersEpoRemovalAddress";
    private static final String WHO_IS_EXCLUDED_FIELD = "manageOrdersWhoIsExcluded";
    private static final String EPO_TYPE_FIELD = "manageOrdersEpoType";
    private static final String EXCLUSION_REQUIREMENT_FIELD = "manageOrdersExclusionRequirement";

    @Override
    public OrderQuestionBlock accept() {
        return EPO_TYPE_AND_PREVENT_REMOVAL;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        if (!isNull(caseData.getOrders())) {
            data.put(REMOVAL_ADDRESS_FIELD, caseData.getOrders().getAddress());
            data.put(EPO_TYPE_FIELD, caseData.getOrders().getEpoType());

            if (EPOType.PREVENT_REMOVAL == caseData.getOrders().getEpoType()
                && isNotEmpty(caseData.getOrders().getExcluded())) {
                data.put(EXCLUSION_REQUIREMENT_FIELD, YES.getValue());
                data.put(WHO_IS_EXCLUDED_FIELD, caseData.getOrders().getExcluded());
            }
        }

        return data;
    }
}
