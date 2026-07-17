package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.LegalRepresentativeRole;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Value
@Builder(toBuilder = true)
public class LegalRepresentative {

    @CCD(label = "Full name")
    String fullName;
    @CCD(label = "Role")
    LegalRepresentativeRole role;
    @CCD(label = "Organisation")
    String organisation;
    @CCD(label = "Email address", typeOverride = FieldType.Email)
    String email;
    @CCD(label = "Phone number")
    String telephoneNumber;

}
