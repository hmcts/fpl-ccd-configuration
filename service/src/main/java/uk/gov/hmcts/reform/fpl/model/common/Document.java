package uk.gov.hmcts.reform.fpl.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.interfaces.UploadDocumentsGroup;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasAttachedDocument;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@HasAttachedDocument(groups = UploadDocumentsGroup.class)
@AllArgsConstructor
public class Document {
    private final String statusReason;
    @NotBlank(message = "Tell us the status of all documents including those that you haven't uploaded")
    private final String documentStatus;
    private final DocumentReference typeOfDocument;
}
