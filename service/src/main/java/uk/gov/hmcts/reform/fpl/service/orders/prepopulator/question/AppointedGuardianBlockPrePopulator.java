package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.AppointedGuardianService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AppointedGuardianBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    private final AppointedGuardianService appointedGuardianService;

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
            appointedGuardianService.getGuardiansLabel(caseData)
        );
    }
}
