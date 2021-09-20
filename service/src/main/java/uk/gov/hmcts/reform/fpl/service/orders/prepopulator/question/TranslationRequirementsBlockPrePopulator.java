package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.TRANSLATION_REQUIREMENTS;

@Component
public class TranslationRequirementsBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    @Override
    public OrderQuestionBlock accept() {
        return TRANSLATION_REQUIREMENTS;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        YesNo caseLanguageRequirement = YesNo.fromString(caseData.getLanguageRequirement());
        OrderTempQuestions orderTempQuestions = caseData.getManageOrdersEventData().getOrderTempQuestions();

        return Map.of("orderTempQuestions", orderTempQuestions.toBuilder()
            .translationRequirements(caseLanguageRequirement == YesNo.YES ? "YES" : "NO")
            .build());
    }
}
