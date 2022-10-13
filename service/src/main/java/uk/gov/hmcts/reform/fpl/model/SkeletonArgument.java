package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class SkeletonArgument extends HearingDocument {
    private final String partyName;
    private final UUID partyId;
    private final UUID hearingId;

    @Builder(toBuilder = true)
    public SkeletonArgument(DocumentReference document,
                                      LocalDateTime dateTimeUploaded,
                                      String uploadedBy,
                                      UUID hearingId,
                                      String hearing,
                                      String partyName,
                                      UUID partyId,
                                      String hasConfidentialAddress) {
        super.dateTimeUploaded = dateTimeUploaded;
        super.uploadedBy = uploadedBy;
        super.hearing = hearing;
        super.document = document;
        super.hasConfidentialAddress = hasConfidentialAddress;
        this.partyName = partyName;
        this.partyId = partyId;
        this.hearingId = hearingId;
    }
}
