package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.SecureAccommodationType;
import uk.gov.hmcts.reform.fpl.enums.SupplementType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Supplement {
    private final SupplementType name;
    private final SecureAccommodationType secureAccommodationType;
    private final String notes;
    private LocalDateTime dateTimeUploaded;
    private final DocumentReference document;
    private String uploadedBy;
    private List<String> documentAcknowledge;

    public List<String> getDocumentAcknowledge() {
        if (this.documentAcknowledge == null) {
            this.documentAcknowledge = new ArrayList<>();
        }
        String acknowledgement = "ACK_RELATED_TO_CASE";
        if (document != null && !this.documentAcknowledge.contains(acknowledgement)) {
            this.documentAcknowledge.add(acknowledgement);
        }
        return this.documentAcknowledge;
    }
}
