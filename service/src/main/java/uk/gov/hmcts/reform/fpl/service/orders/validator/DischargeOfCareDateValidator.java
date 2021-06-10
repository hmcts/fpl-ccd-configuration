package uk.gov.hmcts.reform.fpl.service.orders.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.DISCHARGE_DETAILS;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DischargeOfCareDateValidator implements QuestionBlockOrderValidator {

    private static final String BEFORE_ISSUED_DATE_MESSAGE = "Date of issue cannot be in the future";
    private final Time time;

    @Override
    public OrderQuestionBlock accept() {
        return DISCHARGE_DETAILS;
    }

    @Override
    public List<String> validate(CaseData caseData) {
        final ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();
        final LocalDate issuedDate = manageOrdersEventData.getManageOrdersCareOrderIssuedDate();
        LocalDate now = time.now().toLocalDate();

        List<String> errors = new ArrayList<>();

        if (issuedDate.isAfter(now)) {
            errors.add(BEFORE_ISSUED_DATE_MESSAGE);
        }

        return errors;
    }
}
