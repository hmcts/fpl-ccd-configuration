package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Value
@Builder(toBuilder = true)
public class LegalCounsellor {

    @CCD(label = "First name")
    String firstName;
    @CCD(label = "Last name")
    String lastName;
    @CCD(label = "Email address", typeOverride = FieldType.Email)
    String email;
    @CCD(label = "Phone number")
    String telephoneNumber;
    @CCD(label = "Organisation")
    Organisation organisation;
    @CCD(label = "User id", showCondition = "organisation = \"DO_NOT_SHOW\"")
    String userId;

    @JsonIgnore
    public String getFullName() {
        return firstName + " " + lastName;
    }

}
