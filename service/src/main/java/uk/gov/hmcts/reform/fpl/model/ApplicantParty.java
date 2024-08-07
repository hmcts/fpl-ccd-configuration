package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.interfaces.TelephoneContacts;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasContactDirection;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasTelephoneOrMobile;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@HasTelephoneOrMobile
@HasContactDirection
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString(callSuper = true)
public class ApplicantParty extends Party implements TelephoneContacts {

    private final Telephone mobileNumber;
    @NotBlank(message = "Enter a job title for the contact")
    private final String jobTitle;
    @ToString.Exclude
    @NotBlank(message = "Enter a PBA number for the contact")
    private final String pbaNumber;
    private final String clientCode;
    private final String customerReference;
    private final String legalTeamManager;

    @Override
    @NotBlank(message = "Enter the applicant's full name")
    public String getOrganisationName() {
        return super.getOrganisationName();
    }

    @Override
    @NotNull(message = "Enter a valid address for the contact")
    @Valid
    public Address getAddress() {
        return address;
    }

    @Override
    @NotNull(message = "Enter an email address for the contact")
    @Valid
    public EmailAddress getEmail() {
        return email;
    }

    @Builder(toBuilder = true)
    @SuppressWarnings("java:S107")
    private ApplicantParty(String partyId,
                           PartyType partyType,
                           String firstName,
                           String lastName,
                           String organisationName,
                           LocalDate dateOfBirth,
                           Address address,
                           EmailAddress email,
                           Telephone telephoneNumber,
                           Telephone mobileNumber,
                           String jobTitle,
                           String pbaNumber,
                           String clientCode,
                           String customerReference,
                           String legalTeamManager) {
        super(partyId, partyType, firstName, lastName, organisationName, dateOfBirth, address,
            email, telephoneNumber);

        this.mobileNumber = mobileNumber;
        this.jobTitle = jobTitle;
        this.pbaNumber = pbaNumber;
        this.clientCode = clientCode;
        this.customerReference = customerReference;
        this.legalTeamManager = legalTeamManager;
    }
}
