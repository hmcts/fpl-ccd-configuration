package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;

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
        OrderTempQuestions.OrderTempQuestionsBuilder orderTempQuestionsBuilder = caseData.getManageOrdersEventData()
            .getOrderTempQuestions()
            .toBuilder();
        if (!applicationsToLink.getListItems().isEmpty()) {
            orderTempQuestionsBuilder.linkApplication("YES");
            prePopulatedFields.put("manageOrdersLinkedApplication", applicationsToLink);
        } else {
            orderTempQuestionsBuilder.linkApplication("NO");
        }
        prePopulatedFields.put("orderTempQuestions", orderTempQuestionsBuilder.build());

        return prePopulatedFields;
    }

}
