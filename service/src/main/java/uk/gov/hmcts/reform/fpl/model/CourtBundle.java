package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class CourtBundle extends HearingDocument {
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
        super.hearing = hearing;
        super.document = document;
        this.confidential = confidential;
    }

    @JsonIgnore
    public boolean isConfidentialDocument() {
        return confidential != null && confidential.contains("CONFIDENTIAL");
    }
}
