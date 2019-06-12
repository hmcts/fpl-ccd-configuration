package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
    private final String telephoneNumber;
}
