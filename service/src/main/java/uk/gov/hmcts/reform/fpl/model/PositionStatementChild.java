package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@EqualsAndHashCode(callSuper = true)
public class PositionStatementChild extends HearingDocument {
    private final String childName;
    private final UUID childId;

    @Builder(toBuilder = true)
    public PositionStatementChild(DocumentReference document,
                                  LocalDateTime dateTimeUploaded,
                                  String uploadedBy,
                                  String hearing,
                                  String childName,
                                  UUID childId) {
        super(document, dateTimeUploaded, uploadedBy, hearing);
        this.childName = childName;
        this.childId = childId;
    }
}
