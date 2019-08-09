package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasTelephone;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@HasTelephone
public class Applicant {

    private final String applicantLabel;
    @NotBlank(message = "Enter the applicant's full name")
    private final String name;
    @NotBlank(message = "Enter the contact's full name")
    private final String personToContact;
    @NotBlank(message = "Enter a job title for the contact")
    private final String jobTitle;
    @NotNull(message = "Enter an address for the contact")
    @Valid
    private final Address address;
    private final String mobile;
    private final String telephone;

    @NotBlank(message = "Enter an email address for the contact")
    private final String email;
    private String pbaNumber;

    @JsonCreator
    public Applicant(@JsonProperty("applicantLabel") final String applicantLabel,
                     @JsonProperty("name") final String name,
                     @JsonProperty("personToContact") final String personToContact,
                     @JsonProperty("jobTitle") final String jobTitle,
                     @JsonProperty("address") final Address address,
                     @JsonProperty("mobile") final String mobile,
                     @JsonProperty("telephone") final String telephone,
                     @JsonProperty("email") final String email,
                     @JsonProperty("pbaNumber") final String pbaNumber) {
        this.applicantLabel = applicantLabel;
        this.name = name;
        this.personToContact = personToContact;
        this.jobTitle = jobTitle;
        this.address = address;
        this.mobile = mobile;
        this.telephone = telephone;
        this.email = email;
        this.pbaNumber = pbaNumber;
    }

    public void setPbaNumber(String pbaNumber) {
        this.pbaNumber = pbaNumber;
    }
}
