package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ApplicantsListGenerator;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;

@Component
public class NonMolestationOrderPrePopulator implements QuestionBlockOrderPrePopulator {
    @Autowired
    private ApplicantsListGenerator applicantsListGenerator;

    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.NON_MOLESTATION_ORDER;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        return Map.of(
            "manageOrdersNonMolestationOrderApplicant",
            applicantsListGenerator.buildApplicantsList(caseData, false),
            "manageOrdersNonMolestationOrderRespondent",
            asDynamicList(caseData.getRespondents1(), respondent -> respondent.getParty().getFullName())
        );
    }
}
