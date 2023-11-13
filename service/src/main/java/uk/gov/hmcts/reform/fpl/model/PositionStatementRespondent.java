package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class PositionStatementRespondent extends HearingDocument {
    private final String respondentName;
    private final UUID respondentId;
    private final UUID hearingId;

    @Builder(toBuilder = true)
    public PositionStatementRespondent(DocumentReference document,
                                       LocalDateTime dateTimeUploaded,
                                       String uploadedBy,
                                       UUID hearingId,
                                       String hearing,
                                       String respondentName,
                                       UUID respondentId,
                                       String hasConfidentialAddress) {
        super.dateTimeUploaded = dateTimeUploaded;
        super.uploadedBy = uploadedBy;
        super.hearing = hearing;
        super.document = document;
        super.hasConfidentialAddress = hasConfidentialAddress;
        this.respondentName = respondentName;
        this.respondentId = respondentId;
        this.hearingId = hearingId;
    }
}
