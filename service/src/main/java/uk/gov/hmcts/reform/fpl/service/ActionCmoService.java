package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ActionCmoService {
    private final ObjectMapper objectMapper;
    private final DraftCMOService draftCMOService;
    private final DateFormatterService dateFormatterService;

    //TODO: this should all exist in one CaseManagementOrderService
    @Autowired
    public ActionCmoService(ObjectMapper objectMapper,
                            DraftCMOService draftCMOService,
                            DateFormatterService dateFormatterService) {
        this.objectMapper = objectMapper;
        this.draftCMOService = draftCMOService;
        this.dateFormatterService = dateFormatterService;
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

    private DocumentReference buildDocumentReference(final Document updatedDocument) {
        return DocumentReference.builder()
            .url(updatedDocument.links.self.href)
            .binaryUrl(updatedDocument.links.binary.href)
            .filename(updatedDocument.originalDocumentName)
            .build();
    }

    public String formatHearingBookingLabel(HearingBooking hearingBooking) {
        LocalDateTime startDate = hearingBooking.getStartDate();

        String date = dateFormatterService.formatLocalDateTimeBaseUsingFormat(startDate, "d MMMM");
        String time = dateFormatterService.formatLocalDateTimeBaseUsingFormat(startDate, "h:mma");

        return String.format("The next hearing date is on %s at %s", date, time);
    }
}
