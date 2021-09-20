package uk.gov.hmcts.reform.fpl.service.orders.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.MANAGE_ORDER_END_DATE_WITH_END_OF_PROCEEDINGS;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageOrderEndDateWithEndOfProceedingsValidator implements QuestionBlockOrderValidator {

    private final ManageOrderEndDateCommonValidator manageOrderEndDateCommonValidator;

    @Override
    public OrderQuestionBlock accept() {
        return MANAGE_ORDER_END_DATE_WITH_END_OF_PROCEEDINGS;
    }

    @Override
    public List<String> validate(CaseData caseData) {
        final ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();
        final ManageOrdersEndDateType type = manageOrdersEventData.getManageOrdersEndDateTypeWithEndOfProceedings();
        final LocalDate endDate = manageOrdersEventData.getManageOrdersSetDateEndDate();
        final LocalDateTime endDateTime = manageOrdersEventData.getManageOrdersSetDateAndTimeEndDate();
        final LocalDateTime approvalDateTime = manageOrdersEventData.getManageOrdersApprovalDateOrDateTime();

        return manageOrderEndDateCommonValidator.validate(type, endDate, endDateTime, approvalDateTime, 0);
    }

}
