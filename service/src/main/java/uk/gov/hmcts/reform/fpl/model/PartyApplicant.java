package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.common.TelephoneNumber;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class PartyApplicant extends Party {
    private final String name;
    private final TelephoneNumber mobileNumber;
    private final String jobTitle;
    @JsonProperty("pbaNumber")
    private final String pbaNumber;

    @Builder
    private PartyApplicant(String partyID,
                           String idamID,
                           String partyType,
                           String title,
                           String firstName,
                           String lastName,
                           String organisationName,
                           Date dateOfBirth,
                           Address address,
                           EmailAddress email,
                           TelephoneNumber telephoneNumber,
                           String name,
                           TelephoneNumber mobileNumber,
                           String jobTitle,
                           String pbaNumber) {
        super(partyID, idamID, partyType, title, firstName, lastName, organisationName, dateOfBirth, address, email,
            telephoneNumber);

        this.name = name;
        this.mobileNumber = mobileNumber;
        this.jobTitle = jobTitle;
        this.pbaNumber = pbaNumber;
    }
}
