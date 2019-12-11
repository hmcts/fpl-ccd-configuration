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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;

@Service
public class ActionCmoService {
    private final DraftCMOService draftCMOService;
    private final DateFormatterService dateFormatterService;
    private final HearingBookingService hearingBookingService;

    private static final String LA_CMO_KEY = "caseManagementOrder";
    private static final String JUDGE_CMO_KEY = "cmoToAction";

    //TODO: this should all exist in one CaseManagementOrderService
    @Autowired
    public ActionCmoService(DraftCMOService draftCMOService,
                            DateFormatterService dateFormatterService,
                            HearingBookingService hearingBookingService) {
        this.draftCMOService = draftCMOService;
        this.dateFormatterService = dateFormatterService;
        this.hearingBookingService = hearingBookingService;
    }

    public CaseManagementOrder addDocument(CaseManagementOrder caseManagementOrder, Document document) {
        return caseManagementOrder.toBuilder()
            .orderDoc(buildFromDocument(document))
            .build();
    }

    // REFACTOR: 10/12/2019 Method name
    public void progressCMOToAction(CaseDetails caseDetails, CaseManagementOrder order, boolean approved) {
        switch (order.getAction().getType()) {
            case SEND_TO_ALL_PARTIES:
                caseDetails.getData().put("sharedDraftCMODocument", order.getOrderDoc());
                break;
            case JUDGE_REQUESTED_CHANGE:
                caseDetails.getData().put(LA_CMO_KEY, order);
                caseDetails.getData().remove(JUDGE_CMO_KEY);
                break;
            default:
                break;
        }
    }

    public Map<String, Object> extractMapFieldsFromCaseManagementOrder(CaseManagementOrder order,
                                                                       List<Element<HearingBooking>> hearingDetails) {
        return draftCMOService.extractIndividualCaseManagementOrderObjects(order, hearingDetails);
    }

    public CaseManagementOrder appendNextHearingDateToCMO(DynamicList list, CaseManagementOrder order) {
        CaseManagementOrder.CaseManagementOrderBuilder builder = order.toBuilder();

        if (list != null) {
            HearingDateDynamicElement hearingDateDynamicElement = hearingBookingService.getHearingDynamicElement(list);

            builder
                .action(OrderAction.builder()
                    .nextHearingId(hearingDateDynamicElement.getId())
                    .nextHearingDate(hearingDateDynamicElement.getDate())
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
