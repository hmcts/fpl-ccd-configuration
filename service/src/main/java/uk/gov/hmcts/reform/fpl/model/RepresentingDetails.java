package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Jacksonized
@Builder(toBuilder = true)
public class RepresentingDetails {

    @CCD(label = "First Name")
    private String firstName;
    @CCD(label = "Last Name")
    private String lastName;

    @JsonIgnore
    public String getFullName() {
        if (firstName != null || lastName != null) {
            return firstName + " " + lastName;
        } else {
            return "";
        }
    }
}
