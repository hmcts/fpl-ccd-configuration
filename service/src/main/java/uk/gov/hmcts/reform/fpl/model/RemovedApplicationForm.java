package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class RemovedApplicationForm {

    @CCD(label = "Application form", typeOverride = FieldType.Document)
    DocumentReference submittedForm;
    @CCD(label = "Supplement", typeOverride = FieldType.Document)
    DocumentReference submittedSupplement;
    @CCD(label = "Removal reason", typeOverride = FieldType.TextArea)
    String removalReason;

}
