package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DocumentType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ApplicationDocument {
    private final DocumentReference document;
    private final DocumentType documentType;
    protected LocalDateTime dateTimeUploaded;
    private String uploadedBy;
    private String documentName;
    private String swetDescription;
}
