package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Address {
    @JsonProperty("AddressLine1")
    private final String addressLine1;
    @JsonProperty("AddressLine2")
    private final String addressLine2;
    @JsonProperty("AddressLine3")
    private final String addressLine3;
    @JsonProperty("PostTown")
    private final String postTown;
    @JsonProperty("County")
    private final String county;
    @JsonProperty("PostCode")
    private final String postcode;
    @JsonProperty("Country")
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
