package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingDateDynamicElement;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;

//TODO: this class will take some of the methods out of draftCMO service.
@Service
public class CaseManagementOrderService {
    private final DateFormatterService dateFormatterService;
    private final HearingBookingService hearingBookingService;

    public static final String SHARED_DRAFT_CMO_DOCUMENT_KEY = "sharedDraftCMODocument";
    private static final String LA_CMO_KEY = "caseManagementOrder";
    private static final String JUDGE_CMO_KEY = "cmoToAction";

    @Autowired
    public CaseManagementOrderService(DateFormatterService dateFormatterService,
                            HearingBookingService hearingBookingService) {
        this.dateFormatterService = dateFormatterService;
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

    public void progressCMOToAction(CaseDetails caseDetails, CaseManagementOrder order) {
        switch (order.getAction().getType()) {
            case SEND_TO_ALL_PARTIES:
                caseDetails.getData().put(SHARED_DRAFT_CMO_DOCUMENT_KEY, order.getOrderDoc());
                break;
            case JUDGE_REQUESTED_CHANGE:
                caseDetails.getData().put(LA_CMO_KEY, order);
                caseDetails.getData().remove(JUDGE_CMO_KEY);
                break;
            default:
                break;
        }
    }

    public Map<String, Object> extractMapFieldsFromCaseManagementOrder(CaseManagementOrder order) {
        if (isNull(order)) {
            order = CaseManagementOrder.builder().build();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("schedule", order.getSchedule());
        data.put("recitals", order.getRecitals());
        data.put("orderAction", order.getAction());

        return data;
    }

    public CaseManagementOrder buildCMOWithHearingDate(DynamicList list, CaseManagementOrder order) {
        CaseManagementOrder.CaseManagementOrderBuilder builder = order.toBuilder();

        if (list != null) {
            HearingDateDynamicElement hearingDateDynamicElement = hearingBookingService.getHearingDynamicElement(list);

            builder
                .action(OrderAction.builder()
                    .nextHearingId(hearingDateDynamicElement.getId())
                    .nextHearingDate(hearingDateDynamicElement.getDate())
                    .type(order.getAction().getType())
                    .build())
                .build();
        }

        return builder.build();
    }

    public String createNextHearingDateLabel(CaseManagementOrder caseManagementOrder,
                                             List<Element<HearingBooking>> hearingBookings) {
        String nextHearingLabel = "";

        if (caseManagementOrder != null && caseManagementOrder.getAction() != null) {
            UUID nextHearingId = caseManagementOrder.getAction().getNextHearingId();

            HearingBooking hearingBooking =
                hearingBookingService.getHearingBookingByUUID(hearingBookings, nextHearingId);

            nextHearingLabel = formatHearingBookingLabel(hearingBooking.getStartDate());
        }

        return nextHearingLabel;
    }

    private String formatHearingBookingLabel(LocalDateTime startDate) {
        String date = dateFormatterService.formatLocalDateTimeBaseUsingFormat(startDate, "d MMMM");
        String time = dateFormatterService.formatLocalDateTimeBaseUsingFormat(startDate, "h:mma");

        return String.format("The next hearing date is on %s at %s", date, time);
    }
}
