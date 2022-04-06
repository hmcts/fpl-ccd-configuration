package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
public class CaseSummary extends HearingDocument {

    @Builder(toBuilder = true)
    public CaseSummary(DocumentReference document,
                       LocalDateTime dateTimeUploaded,
                       String uploadedBy,
                       String hearing) {
        super(document, dateTimeUploaded, uploadedBy, hearing);
    }
}
