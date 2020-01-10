package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.validation.groups.EpoOrderGroup.EPOAddressGroup;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Address {
    @NotBlank(message = "Enter a valid address for the contact", groups = { EPOAddressGroup.class })
    private final String addressLine1;
    private final String addressLine2;
    private final String addressLine3;
    private final String postTown;
    private final String county;
    @NotBlank(message = "Enter a postcode for the contact", groups = { EPOAddressGroup.class })
    private final String postcode;
    private final String country;

    @JsonCreator
    public Address(@JsonProperty("AddressLine1") final String addressLine1,
                   @JsonProperty("AddressLine2") final String addressLine2,
                   @JsonProperty("AddressLine3") final String addressLine3,
                   @JsonProperty("PostTown") final String postTown,
                   @JsonProperty("County") final String county,
                   @JsonProperty("PostCode") final String postcode,
                   @JsonProperty("Country") final String country) {
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.addressLine3 = addressLine3;
        this.postTown = postTown;
        this.county = county;
        this.postcode = postcode;
        this.country = country;
    }
}
