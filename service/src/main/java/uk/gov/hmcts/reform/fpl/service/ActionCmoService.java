package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingDateDynamicElement;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ActionCmoService {
    private final ObjectMapper objectMapper;
    private final DraftCMOService draftCMOService;
    private final DateFormatterService dateFormatterService;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final HearingBookingService hearingBookingService;

    //TODO: this should all exist in one CaseManagementOrderService
    @Autowired
    public ActionCmoService(ObjectMapper objectMapper,
                            DraftCMOService draftCMOService,
                            DateFormatterService dateFormatterService,
                            HearingVenueLookUpService hearingVenueLookUpService,
                            HearingBookingService hearingBookingService) {
        this.objectMapper = objectMapper;
        this.draftCMOService = draftCMOService;
        this.dateFormatterService = dateFormatterService;
        this.hearingVenueLookUpService = hearingVenueLookUpService;
        this.hearingBookingService = hearingBookingService;
    }

    public CaseManagementOrder addDocument(CaseManagementOrder caseManagementOrder, Document document) {
        return CaseManagementOrder.builder()
            .orderDoc(buildDocumentReference(document))
            .build();
    }

    public CaseManagementOrder getCaseManagementOrderForAction(Map<String, Object> caseDataMap) {
        CaseData caseData = objectMapper.convertValue(caseDataMap, CaseData.class);

        caseDataMap.putAll(draftCMOService.extractIndividualCaseManagementOrderObjects(
            caseData.getCaseManagementOrder(), caseData.getHearingDetails()));

        return objectMapper.convertValue(caseDataMap.get("caseManagementOrder"), CaseManagementOrder.class);
    }

    public CaseManagementOrder appendNextHearingDateToCMO(DynamicList list, CaseManagementOrder order) {
        CaseManagementOrder.CaseManagementOrderBuilder builder = order.toBuilder();

        if (list != null) {
            HearingDateDynamicElement hearingDateDynamicElement =
                hearingVenueLookUpService.getHearingDynamicElement(list);

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

            nextHearingLabel = formatHearingBookingLabel(hearingBooking);
        }

        return nextHearingLabel;
    }

    private DocumentReference buildDocumentReference(final Document updatedDocument) {
        return DocumentReference.builder()
            .url(updatedDocument.links.self.href)
            .binaryUrl(updatedDocument.links.binary.href)
            .filename(updatedDocument.originalDocumentName)
            .build();
    }

    private String formatHearingBookingLabel(HearingBooking hearingBooking) {
        LocalDateTime startDate = hearingBooking.getStartDate();

        String date = dateFormatterService.formatLocalDateTimeBaseUsingFormat(startDate, "d MMMM");
        String time = dateFormatterService.formatLocalDateTimeBaseUsingFormat(startDate, "h:mma");

        return String.format("The next hearing date is on %s at %s", date, time);
    }
}
