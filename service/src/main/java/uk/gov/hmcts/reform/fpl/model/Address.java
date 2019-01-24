package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {

    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String postTown;
    private String county;
    private String postcode;
    private String country;

    @JsonCreator
    public Address(@JsonProperty("AddressLine1") final String addressLine1,
                   @JsonProperty("AddressLine2") final String addressLine2,
                   @JsonProperty("AddressLine3") final String addressLine3,
                   @JsonProperty("PostTown") final String postTown,
                   @JsonProperty("County") final String county,
                   @JsonProperty("PostCode") final String postcode,
                   @JsonProperty("Country") final String country
                   ) {
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.addressLine3 = addressLine3;
        this.postTown = postTown;
        this.county = county;
        this.postcode = postcode;
        this.country = country;
    }


    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public String getPostTown() {
        return postTown;
    }

    public String getCounty() {
        return county;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getCountry() {
        return country;
    }



}
