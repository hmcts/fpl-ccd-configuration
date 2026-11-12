package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.ccd.model.Organisation;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class LegalCounsellor {

    String firstName;
    String lastName;
    String email;
    String telephoneNumber;
    Organisation organisation;
    String userId;

    @JsonIgnore
    public String getFullName() {
        return firstName + " " + lastName;
    }

}
