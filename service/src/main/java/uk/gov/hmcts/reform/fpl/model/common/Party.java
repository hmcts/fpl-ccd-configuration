package uk.gov.hmcts.reform.fpl.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.Address;

import java.util.Date;

@Data
@AllArgsConstructor
public class Party {
    private final String partyID;
    private final String partyType;
    private final String title;
    private final String firstName;
    private final String lastName;
    private final String organisationName;
    private final Date dateOfBirth;
    private final Address address;
    private final EmailAddress email;
    private final TelephoneNumber telephoneNumber;
}
