package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OldApplicant {

    private final String applicantLabel;
    private final String name;
    private final String personToContact;
    private final String jobTitle;
    private final Address address;
    private final String mobile;
    private final String telephone;
    private final String email;
    private String pbaNumber;

    @JsonCreator
    public OldApplicant(@JsonProperty("applicantLabel") final String applicantLabel,
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
}
