package uk.gov.hmcts.reform.rd.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganisationUser {
    private String userIdentifier;
    private String email;
    private String firstName;
    private String lastName;

    @JsonIgnore
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @JsonIgnore
    public String getUserString() {
        return "<li>" + getFullName() + " (" + email + ") </li>";
    }
}
