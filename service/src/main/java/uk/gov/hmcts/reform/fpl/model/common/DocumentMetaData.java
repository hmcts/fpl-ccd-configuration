package uk.gov.hmcts.reform.fpl.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class DocumentMetaData {
    protected final DocumentReference typeOfDocument;
    protected LocalDateTime dateTimeUploaded;
    protected String uploadedBy;
}
