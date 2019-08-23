package uk.gov.hmcts.reform.fpl.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
public class Document {
    private final String statusReason;
    @NotBlank(message = "Tell us the status of all documents including those that you haven't uploaded")
    private final String documentStatus;
    private final DocumentReference typeOfDocument;
}
