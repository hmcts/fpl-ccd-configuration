package uk.gov.hmcts.reform.fpl.model.common;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class DocumentSocialWorkOther extends DocumentMetaData {
    private final String documentTitle;

    public DocumentSocialWorkOther(DocumentReference typeOfDocument,
                                   LocalDateTime dateTimeUploaded,
                                   String uploadedBy,
                                   String documentTitle) {
        super(typeOfDocument, dateTimeUploaded, uploadedBy);
        this.documentTitle = documentTitle;
    }

}
