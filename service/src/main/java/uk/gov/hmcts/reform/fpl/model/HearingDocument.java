package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.enums.HearingDocumentType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentMetaData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class HearingDocument extends DocumentMetaData {
    protected String hearing;
    protected DocumentReference document;

    @JsonIgnore
    @Override
    public DocumentReference getTypeOfDocument() {
        return document;
    }

    public HearingDocument(DocumentReference document,
                       LocalDateTime dateTimeUploaded,
                       String uploadedBy,
                       String hearing) {
        super.dateTimeUploaded = dateTimeUploaded;
        super.uploadedBy = uploadedBy;
        this.hearing = hearing;
        this.document = document;
    }
}
