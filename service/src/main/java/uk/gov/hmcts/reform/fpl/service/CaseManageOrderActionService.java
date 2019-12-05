package uk.gov.hmcts.reform.fpl.service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;

@Slf4j
@Service
public class CaseManageOrderActionService {
    private final DateFormatterService dateFormatterService;

    @Autowired
    public CaseManageOrderActionService(DateFormatterService dateFormatterService) {
        this.dateFormatterService = dateFormatterService;
    }

    public CaseManagementOrder addDocumentToCaseManagementOrder(final CaseManagementOrder caseManagementOrder,
                                                                final Document documentToAdd) {
        return caseManagementOrder.toBuilder()
            .orderDoc(buildCMODocumentReference(documentToAdd))
            .build();
    }

    public String formatHearingBookingLabel(HearingBooking hearingBooking) {
        LocalDateTime startDate = hearingBooking.getStartDate();

        String date = dateFormatterService.formatLocalDateTimeBaseUsingFormat(startDate, "d MMMM");
        String time = dateFormatterService.formatLocalDateTimeBaseUsingFormat(startDate, "h:mma");

        return String.format("The next hearing date is on %s at %s", date, time);
    }

    private DocumentReference buildCMODocumentReference(final Document updatedDocument) {
        return DocumentReference.builder()
            .url(updatedDocument.links.self.href)
            .binaryUrl(updatedDocument.links.binary.href)
            .filename(updatedDocument.originalDocumentName)
            .build();
    }
}
