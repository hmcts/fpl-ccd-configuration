package uk.gov.hmcts.reform.fpl.model.noc;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Value
@Builder
public class ChangedRepresentative {
    @CCD(label = "First name")
    String firstName;
    @CCD(label = "Last name")
    String lastName;
    @CCD(label = "Email", typeOverride = FieldType.Email)
    String email;
    @CCD(label = "Organisation")
    Organisation organisation;
}
