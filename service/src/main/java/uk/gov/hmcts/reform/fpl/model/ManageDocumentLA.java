package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeListLA;

@Data
@Builder(toBuilder = true)
public class ManageDocumentLA {
    private final ManageDocumentTypeListLA type;
    private final String hasHearings; // Hidden CCD field
    private final String hasC2s; // Hidden CCD field
}
