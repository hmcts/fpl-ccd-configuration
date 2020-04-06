package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingDateDynamicElement;
import uk.gov.hmcts.reform.fpl.model.NextHearing;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SCHEDULE;
import static uk.gov.hmcts.reform.fpl.model.HearingDateDynamicElement.getHearingDynamicElement;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.parseLocalDateFromStringUsingFormat;

//TODO: this class will take some of the methods out of draftCMO service. FPLA-1479
@Service
public class CaseManagementOrderService {
    private final Time time;
    private final HearingBookingService hearingBookingService;

    public CaseManagementOrderService(Time time, HearingBookingService hearingBookingService) {
        this.time = time;
        this.hearingBookingService = hearingBookingService;
    }

    public CaseManagementOrder addDocument(CaseManagementOrder caseManagementOrder, Document document) {
        return caseManagementOrder.toBuilder()
            .orderDoc(buildFromDocument(document))
            .build();
    }

    public CaseManagementOrder addAction(CaseManagementOrder order, OrderAction orderAction) {
        return order.toBuilder()
            .action(orderAction)
            .build();
    }

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

    public OrderAction removeDocumentFromOrderAction(OrderAction orderAction) {
        return orderAction.toBuilder().document(null).build();
    }

    public boolean isHearingDateInFuture(CaseData caseData) {
        LocalDateTime hearingDate = hearingBookingService
            .getHearingBookingByUUID(caseData.getHearingDetails(), caseData.getCaseManagementOrder().getId())
            .getStartDate();

        return time.now().isBefore(hearingDate);
    }

    public CaseManagementOrder addNextHearingToCMO(DynamicList list, CaseManagementOrder order) {
        if (list == null) {
            return order;
        }

        HearingDateDynamicElement hearingDateDynamicElement = getHearingDynamicElement(list);

        return order.toBuilder()
            .nextHearing(NextHearing.builder()
                .id(hearingDateDynamicElement.getId())
                .date(hearingDateDynamicElement.getDate())
                .build())
            .build();
    }

    public LocalDate getIssuedDate(CaseManagementOrder caseManagementOrder) {
        if (caseManagementOrder == null || isEmpty(caseManagementOrder.getDateOfIssue())) {
            return time.now().toLocalDate();
        }

        return parseLocalDateFromStringUsingFormat(caseManagementOrder.getDateOfIssue(), DATE);
    }
}
