package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.Address;

import java.util.Date;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Party {
    public final String partyID;
    public final String idamID;
    public final String partyType;
    public final String title;
    public final String firstName;
    public final String lastName;
    public final String organisationName;
    public final Date dateOfBirth;
    public final Address address;
    public final EmailAddress email;
    public final TelephoneNumber telephoneNumber;
}
