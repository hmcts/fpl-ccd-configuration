package uk.gov.hmcts.reform.fpl.model.common;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentSocialWorkOther extends DocumentMetaData {
    private final String documentTitle;

    @Builder(toBuilder = true)
    public DocumentSocialWorkOther(DocumentReference typeOfDocument,
                                   LocalDateTime dateTimeUploaded,
                                   String uploadedBy,
                                   String documentTitle) {
        super(typeOfDocument, dateTimeUploaded, uploadedBy);
        this.documentTitle = documentTitle;
    }
}
