package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.groups.Default;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.validation.groups.epoordergroup.EPOAddressGroup;

import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Address {
    @NotBlank(message = "Enter a valid address for the contact", groups = { Default.class, EPOAddressGroup.class })
    private final String addressLine1;
    private final String addressLine2;
    private final String addressLine3;
    private final String postTown;
    private final String county;
    @NotBlank(message = "Enter a postcode for the contact", groups = { Default.class, EPOAddressGroup.class })
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

    @JsonIgnore
    public String getAddressAsString(String delimiter) {
        ImmutableList<String> addressAsList = ImmutableList.of(
            defaultIfNull(getAddressLine1(), ""),
            defaultIfNull(getAddressLine2(), ""),
            defaultIfNull(getAddressLine3(), ""),
            defaultIfNull(getPostTown(), ""),
            defaultIfNull(getCounty(), ""),
            defaultIfNull(getPostcode(), ""),
            defaultIfNull(getCountry(), ""));

        return addressAsList.stream()
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.joining(delimiter));
    }
}
