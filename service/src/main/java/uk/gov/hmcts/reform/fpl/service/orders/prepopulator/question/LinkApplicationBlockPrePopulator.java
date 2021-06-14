package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.HashMap;
import java.util.Map;

@Component
public class LinkApplicationBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.LINK_APPLICATION;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        Map<String, Object> prePopulatedFields = new HashMap<>();

        DynamicList applicationsToLink = caseData.buildApplicationBundlesDynamicList();
        if (!applicationsToLink.getListItems().isEmpty()) {
            prePopulatedFields.put("linkApplication", "YES");
            prePopulatedFields.put("manageOrdersLinkedApplication", applicationsToLink);
        } else {
            prePopulatedFields.put("linkApplication", "NO");
        }

        return prePopulatedFields;
    }

}
