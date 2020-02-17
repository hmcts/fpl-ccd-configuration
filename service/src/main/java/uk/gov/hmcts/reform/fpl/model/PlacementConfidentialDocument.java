package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlacementConfidentialDocument {
    private PlacementDocumentType type;
    private DocumentReference document;
    private String description;

    public enum PlacementDocumentType {
        ANNEX_B,
        GUARDIANS_REPORT,
        OTHER_CONFIDENTIAL_DOCUMENTS
    }
}
