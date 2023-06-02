package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class CaseSummary extends HearingDocument {

    @Builder(toBuilder = true)
    public CaseSummary(DocumentReference document,
                       LocalDateTime dateTimeUploaded,
                       String uploadedBy,
                       String hearing,
                       String hasConfidentialAddress) {
        super.dateTimeUploaded = dateTimeUploaded;
        super.uploadedBy = uploadedBy;
        super.hearing = hearing;
        super.document = document;
        super.hasConfidentialAddress = hasConfidentialAddress;
    }
}
