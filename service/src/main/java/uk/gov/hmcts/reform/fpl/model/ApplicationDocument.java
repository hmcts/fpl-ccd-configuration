package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.interfaces.FurtherDocument;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ApplicationDocument implements FurtherDocument {
    private final DocumentReference document;
    private final ApplicationDocumentType documentType;
    protected LocalDateTime dateTimeUploaded;
    private String uploadedBy;
    private String documentName;
    private String includedInSWET;

    @JsonIgnore
    public boolean hasDocument() {
        return document != null;
    }

    @JsonIgnore
    public boolean isConfidentialDocument() {
        return true;
    }

    @JsonIgnore
    public String getName() {
        return documentType.getLabel();
    }
}
