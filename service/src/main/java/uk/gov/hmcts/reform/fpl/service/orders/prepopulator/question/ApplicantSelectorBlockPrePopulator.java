package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApplicantSelectorBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.APPLICANT_SELECTOR;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        //TODO add 'others' and change ApplicantSelector.json to work with that
        final Selector applicantSelector = newSelector(caseData.getAllRespondents().size());
        return Map.of(
            "applicantSelector", applicantSelector,
            "applicants_label", getApplicantsLabel(caseData.getAllRespondents())
        );
    }

    //TODO add 'others' to label and put in service maybe
    private String getApplicantsLabel(List<Element<Respondent>> respondents) {
        if (isEmpty(respondents)) {
            return "No respondents in the case";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < respondents.size(); i++) {
            Respondent respondent = respondents.get(i).getValue();

            builder.append(String.format("Respondent %d: %s", i + 1, respondent.getParty().getFullName()));
            builder.append("\n");
        }

        return builder.toString();
    }
}
