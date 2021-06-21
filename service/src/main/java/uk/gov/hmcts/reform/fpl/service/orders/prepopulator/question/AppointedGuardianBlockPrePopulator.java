package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import com.mchange.v2.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AppointedGuardianBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.APPOINTED_GUARDIAN;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        final Selector appointedGuardianSelector = newSelector(
            caseData.getAllRespondents().size() + caseData.getAllOthers().size());
        return Map.of(
            "appointedGuardianSelector", appointedGuardianSelector,
            "appointedGuardians_label",
            getAppointedGuardiansLabel(caseData.getAllRespondents(), caseData.getAllOthers())
        );
    }

    //put in a service maybe
    private String getAppointedGuardiansLabel(List<Element<Respondent>> respondents, List<Element<Other>> others) {
        if (isEmpty(respondents) && isEmpty(others)) {
            return "No respondents or others on the case";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < CollectionUtils.size(respondents); i++) {
            Respondent respondent = respondents.get(i).getValue();

            builder.append(String.format("Respondent %d: %s", i + 1, respondent.getParty().getFullName()));
            builder.append("\n");
        }

        for (int i = 0; i < CollectionUtils.size(others); i++) {
            Other other = others.get(i).getValue();

            builder.append(String.format("Other %d: %s", i + 1, other.getName()));
            builder.append("\n");
        }

        return builder.toString();
    }
}
