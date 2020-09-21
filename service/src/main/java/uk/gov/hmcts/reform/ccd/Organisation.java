package uk.gov.hmcts.reform.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Organisation {

    @JsonProperty("OrganisationID")
    private String organisationID;
    @JsonProperty("OrganisationName")
    private String organisationName;
}
