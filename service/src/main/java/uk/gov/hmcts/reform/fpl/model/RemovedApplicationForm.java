package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class RemovedApplicationForm {

    DocumentReference submittedForm;
    DocumentReference submittedSupplement;
    String removalReason;

}
