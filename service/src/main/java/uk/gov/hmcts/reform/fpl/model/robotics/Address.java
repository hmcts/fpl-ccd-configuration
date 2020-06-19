package uk.gov.hmcts.reform.fpl.model.robotics;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * Same as {@link uk.gov.hmcts.reform.fpl.model.Address}, added this given former has
 * JsonCreator on constructor and @JsonProperty with UpperCamelCase for fields.
 */
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Address {
    private final String addressLine1;
    private final String addressLine2;
    private final String addressLine3;
    private final String postTown;
    private final String county;
    private final String postcode;
    private final String country;
}
