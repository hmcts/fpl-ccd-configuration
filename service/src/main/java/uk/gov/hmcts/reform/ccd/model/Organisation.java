package uk.gov.hmcts.reform.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Organisation {

    @JsonProperty("OrganisationID")
    private String organisationID;

    @JsonProperty("OrganisationName")
    private String organisationName;
}
