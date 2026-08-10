package uk.gov.hmcts.reform.fpl.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@SuperBuilder(toBuilder = true)
public class DocumentMetaData {
    @CCD(label = "Upload a file", categoryID = "archivedDocuments", typeOverride = FieldType.Document)
    protected final DocumentReference typeOfDocument;
    @CCD(label = "Date and time uploaded")
    protected LocalDateTime dateTimeUploaded;
    @CCD(label = "Uploaded by")
    protected String uploadedBy;
}
