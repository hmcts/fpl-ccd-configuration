package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
public class CourtAdminDocument {
    @CCD(
            label = "Document name",
            hint = "Give the document a descriptive name. For example, 'Parenting assessment' or 'Paediatric report'"
    )
    private final String documentTitle;
    @CCD(label = "Upload a file", typeOverride = FieldType.Document)
    private final DocumentReference document;
}
