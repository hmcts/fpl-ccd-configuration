package uk.gov.hmcts.reform.fpl.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DocumentMetaData {
    protected final DocumentReference typeOfDocument;
    protected LocalDateTime dateTimeUploaded;
    protected String uploadedBy;
}
