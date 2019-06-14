package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.fpl.model.common.Email;
import uk.gov.hmcts.reform.fpl.model.common.TelephoneNumber;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Party {
    private final String partyID;
    private final String idamID;
    private final String partyType;
    private final String title;
    private final String firstName;
    private final String lastName;
    private final String organisationName;
    private final Date dateOfBirth;
    private final Address address;
    private final Email email;
    private final TelephoneNumber telephoneNumber;

    @JsonCreator
    public Party(@JsonProperty("partyId") final String partyID,
                 @JsonProperty("idamID") final String idamID,
                 @JsonProperty("partyType") final String partyType,
                 @JsonProperty("title") final String title,
                 @JsonProperty("firstName") final String firstName,
                 @JsonProperty("lastName") final String lastName,
                 @JsonProperty("organisationName") final String organisationName,
                 @JsonProperty("dateOfBirth") final Date dateOfBirth,
                 @JsonProperty("address") final Address address,
                 @JsonProperty("email") final Email email,
                 @JsonProperty("telephoneNumber") final TelephoneNumber telephoneNumber) {
        this.partyID = partyID;
        this.idamID = idamID;
        this.partyType = partyType;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.organisationName = organisationName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.email = email;
        this.telephoneNumber = telephoneNumber;
    }
}
