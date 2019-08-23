package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.interfaces.TelephoneContacts;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasContactDirection;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasTelephoneOrMobile;

import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
@HasTelephoneOrMobile
@HasContactDirection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicantParty extends Party implements TelephoneContacts {
    @NotBlank(message = "Enter the applicant's full name")
    private final String organisationName;
    private final Telephone mobileNumber;
    @NotBlank(message = "Enter a job title for the contact")
    private final String jobTitle;
    private final String pbaNumber;
    @Builder(toBuilder = true)
    private ApplicantParty(String partyId,
                           PartyType partyType,
                           String firstName,
                           String lastName,
                           String organisationName,
                           Date dateOfBirth,
                           @Valid
                           Address address,
                           @Valid
                           EmailAddress email,
                           Telephone telephoneNumber,
                           Telephone mobileNumber,
                           String jobTitle,
                           String pbaNumber) {
        super(partyId, partyType, firstName, lastName, organisationName, dateOfBirth, address,
            email, telephoneNumber);

        this.organisationName = organisationName;
        this.mobileNumber = mobileNumber;
        this.jobTitle = jobTitle;
        this.pbaNumber = pbaNumber;
    }
}
