package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class IssuingDetailsApprovalDateBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    private final Time time;
    private static final String CASE_FIELD_KEY = "manageOrdersApprovalDate";

    @Override
    public OrderQuestionBlock accept() {
        return APPROVAL_DATE;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {

        LocalDate approvalDate = caseData.getManageOrdersEventData().getManageOrdersApprovalDate();

        if (approvalDate == null) {
            return Map.of(CASE_FIELD_KEY, time.now().toLocalDate());
        }
        return Map.of();
    }
}
