package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.model.Organisation;

@Value
@Builder
public class LegalCounsellor {

    String firstName;
    String lastName;
    String email;
    String telephoneNumber;
    Organisation organisation;

    @JsonIgnore
    public String getFullName() {
        return firstName + " " + lastName;
    }

}
