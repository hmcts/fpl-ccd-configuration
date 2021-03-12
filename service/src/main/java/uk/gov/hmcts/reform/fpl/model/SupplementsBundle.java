package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.SecureAccommodationType;
import uk.gov.hmcts.reform.fpl.enums.Supplements;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupplementsBundle {
    private final Supplements name;
    private final SecureAccommodationType secureAccommodationType;
    private final String notes;
    private LocalDateTime dateTimeUploaded;
    private final DocumentReference document;
    private String uploadedBy;
    private List<String> confidential;

    @JsonIgnore
    public boolean isConfidentialDocument() {
        return confidential != null && confidential.contains("CONFIDENTIAL");
    }

    @JsonIgnore
    public boolean isUploadedByHMCTS() {
        return "HMCTS".equals(uploadedBy);
    }

    @JsonGetter("confidentialTabLabel")
    public String generateConfidentialTabLabel() {
        return isConfidentialDocument() ? "Confidential" : null;
    }
}
