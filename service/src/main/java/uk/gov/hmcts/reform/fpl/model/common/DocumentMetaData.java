package uk.gov.hmcts.reform.fpl.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@SuperBuilder(toBuilder = true)
public class DocumentMetaData {
    protected final DocumentReference typeOfDocument;
    protected LocalDateTime dateTimeUploaded;
    protected String uploadedBy;
}
