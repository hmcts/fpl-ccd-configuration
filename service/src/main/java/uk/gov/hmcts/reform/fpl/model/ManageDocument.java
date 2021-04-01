package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentType;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Data
@Builder(toBuilder = true)
public class ManageDocument {
    private final ManageDocumentType type;
    private final String relatedToHearing;
    private final String hasHearings; // Hidden CCD field
    private final String hasC2s; // Hidden CCD field

    @JsonIgnore
    public boolean isDocumentRelatedToHearing() {
        return YES.getValue().equals(relatedToHearing);
    }
}
