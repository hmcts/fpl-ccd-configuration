package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.PastOrPresentDate;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupportingEvidenceBundle {
    private final String name;
    private final String notes;
    @PastOrPresentDate(message = "Date received cannot be in the future")
    private final LocalDateTime dateTimeReceived;
    private LocalDateTime dateTimeUploaded;
    private final DocumentReference document;
    private String uploadedBy;
    private List<String> confidential;

    @JsonIgnore
    public boolean isConfidential() {
        return confidential != null  && confidential.contains("CONFIDENTIAL");
    }

    @JsonIgnore
    public boolean isUploadedByHMCTS() {
        return "HMCTS".equals(uploadedBy);
    }
}
