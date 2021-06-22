package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;

@Component
public class AppointedGuardianValidator implements QuestionBlockOrderValidator {

    private static final String MESSAGE = "Select the appointed guardian for the children";

    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.APPOINTED_GUARDIAN;
    }

    @Override
    public List<String> validate(CaseData caseData) {
        Selector guardianSelector = caseData.getAppointedGuardianSelector();

        if (guardianSelector.getSelected().isEmpty()) {
            return List.of(MESSAGE);
        }

        return List.of();
    }
}
