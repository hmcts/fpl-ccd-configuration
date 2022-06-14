package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.DocumentMetaData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class CourtBundle extends DocumentMetaData {
    private String hearing;
    private DocumentReference document;
    private List<String> confidential;

    @JsonIgnore
    @Override
    public DocumentReference getTypeOfDocument() {
        return document;
    }

    @Builder(toBuilder = true)
    public CourtBundle(DocumentReference document,
                       LocalDateTime dateTimeUploaded,
                       String uploadedBy,
                       String hearing,
                       List<String> confidential) {
        super.dateTimeUploaded = dateTimeUploaded;
        super.uploadedBy = uploadedBy;
        this.confidential = confidential;
        this.hearing = hearing;
        this.document = document;
    }

    @JsonIgnore
    public boolean isConfidentialDocument() {
        return confidential != null && confidential.contains("CONFIDENTIAL");
    }
}
