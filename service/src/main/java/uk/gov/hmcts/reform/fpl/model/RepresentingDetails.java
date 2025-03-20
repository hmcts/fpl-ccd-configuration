package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder(toBuilder = true)
public class RepresentingDetails {

    private String firstName;
    private String lastName;

    @JsonIgnore
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
