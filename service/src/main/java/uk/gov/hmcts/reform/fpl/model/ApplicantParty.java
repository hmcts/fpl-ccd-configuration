package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
                           String customerReference) {
        super(partyId, partyType, firstName, lastName, organisationName, dateOfBirth, address,
            email, telephoneNumber);

        this.mobileNumber = mobileNumber;
        this.jobTitle = jobTitle;
        this.pbaNumber = pbaNumber;
        this.clientCode = clientCode;
        this.customerReference = customerReference;
    }
}
