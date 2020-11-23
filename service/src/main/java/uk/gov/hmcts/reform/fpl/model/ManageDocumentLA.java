package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeLA;

@Data
@Builder(toBuilder = true)
public class ManageDocumentLA extends ManageDocument {
    private final String label;
    private final ManageDocumentTypeLA type;
}
