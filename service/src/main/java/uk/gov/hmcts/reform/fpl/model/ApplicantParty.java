package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicantParty extends Party {
    private final String organisationName;
    private final Telephone mobileNumber;
    private final String jobTitle;
    private final String pbaNumber;

    @Builder(toBuilder = true)
    private ApplicantParty(String partyId,
                           String idamID,
                           PartyType partyType,
                           String title,
                           String firstName,
                           String lastName,
                           String organisationName,
                           Date dateOfBirth,
                           Address address,
                           EmailAddress email,
                           Telephone telephoneNumber,
                           Telephone mobileNumber,
                           String jobTitle,
                           String pbaNumber) {
        super(partyId, idamID, partyType, title, firstName, lastName, organisationName, dateOfBirth, address,
            email, telephoneNumber);

        this.organisationName = organisationName;
        this.mobileNumber = mobileNumber;
        this.jobTitle = jobTitle;
        this.pbaNumber = pbaNumber;
    }
}
