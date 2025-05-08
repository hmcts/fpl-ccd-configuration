package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.AppointedGuardianFormatter;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AppointedGuardianBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    private final AppointedGuardianFormatter appointedGuardianFormatter;

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
            appointedGuardianFormatter.getGuardiansLabel(caseData)
        );
    }
}
