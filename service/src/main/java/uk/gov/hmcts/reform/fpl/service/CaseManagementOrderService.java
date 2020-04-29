package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SCHEDULE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.parseLocalDateFromStringUsingFormat;

//TODO: this class will take some of the methods out of draftCMO service. FPLA-1479
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderService {
    private final Time time;

    public Map<String, Object> extractMapFieldsFromCaseManagementOrder(CaseManagementOrder order) {
        if (isNull(order)) {
            order = CaseManagementOrder.builder().build();
        }

        Map<String, Object> data = new HashMap<>();
        data.put(SCHEDULE.getKey(), order.getSchedule());
        data.put(RECITALS.getKey(), order.getRecitals());
        data.put(ORDER_ACTION.getKey(), order.getAction());

        return data;
    }

    public LocalDate getIssuedDate(CaseManagementOrder caseManagementOrder) {
        if (caseManagementOrder == null || isEmpty(caseManagementOrder.getDateOfIssue())) {
            return time.now().toLocalDate();
        }

        return parseLocalDateFromStringUsingFormat(caseManagementOrder.getDateOfIssue(), DATE);
    }
}
