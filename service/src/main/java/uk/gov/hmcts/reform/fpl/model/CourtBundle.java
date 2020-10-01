package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.DocumentMetaData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class CourtBundle extends DocumentMetaData {

    public CourtBundle(DocumentReference document, LocalDateTime dateTimeUploaded, String uploadedBy) {
        super(document, dateTimeUploaded, uploadedBy);
    }

    @JsonProperty("document")
    @Override
    public DocumentReference getTypeOfDocument() {
        return super.getTypeOfDocument();
    }
}
