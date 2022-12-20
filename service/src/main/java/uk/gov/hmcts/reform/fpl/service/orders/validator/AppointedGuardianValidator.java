package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;

@Component
public class AppointedGuardianValidator implements QuestionBlockOrderValidator {

    private static final String MESSAGE = "Select the appointed guardian for the children from the"
        + " list of parties or detail the special guardians in the free text field. ";

    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.APPOINTED_GUARDIAN;
    }

    @Override
    public List<String> validate(CaseData caseData) {
        Selector guardianSelector = caseData.getAppointedGuardianSelector();
        String additionalSpecialGuardians = caseData.getAdditionalAppointedSpecialGuardians();
        if (guardianSelector.getSelected().isEmpty() && StringUtils.isEmpty(additionalSpecialGuardians)) {
            return List.of(MESSAGE);
        }
        return List.of();
    }
}
