package uk.gov.hmcts.reform.fpl.model.common;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.validation.groups.UploadDocumentsGroup;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasAttachedDocument;

import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@Data
@HasAttachedDocument(groups = UploadDocumentsGroup.class)
public class Document extends DocumentMetaData {
    private final String statusReason;
    @NotBlank(message = "Tell us the status of all documents including those that you haven't uploaded")
    private final String documentStatus;

    @Builder(toBuilder = true)
    public Document(DocumentReference typeOfDocument,
                    LocalDateTime dateTimeUploaded,
                    String uploadedBy,
                    String statusReason,
                    String documentStatus) {
        super(typeOfDocument, dateTimeUploaded, uploadedBy);
        this.statusReason = statusReason;
        this.documentStatus = documentStatus;
    }
}
