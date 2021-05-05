package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentType;

@Data
@Builder(toBuilder = true)
public class ManageDocument {
    private final ManageDocumentType type;
    private final String hasHearings; // Hidden CCD field
    private final String hasC2s; // Hidden CCD field
}
