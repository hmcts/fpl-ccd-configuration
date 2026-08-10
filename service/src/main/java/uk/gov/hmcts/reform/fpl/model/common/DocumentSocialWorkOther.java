package uk.gov.hmcts.reform.fpl.model.common;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "OtherDocument", generate = true)
@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class DocumentSocialWorkOther extends DocumentMetaData {
    @CCD(
            label = "Document name",
            hint = "Give the document a descriptive name. For example, 'Parenting assessment' or 'Paediatric report'"
    )
    private final String documentTitle;

    public DocumentSocialWorkOther(DocumentReference typeOfDocument,
                                   LocalDateTime dateTimeUploaded,
                                   String uploadedBy,
                                   String documentTitle) {
        super(typeOfDocument, dateTimeUploaded, uploadedBy);
        this.documentTitle = documentTitle;
    }

}
