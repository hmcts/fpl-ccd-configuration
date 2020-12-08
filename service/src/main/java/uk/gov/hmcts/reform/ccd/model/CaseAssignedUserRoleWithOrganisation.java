package uk.gov.hmcts.reform.ccd.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class CaseAssignedUserRoleWithOrganisation {

    @JsonProperty("organisation_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String organisationId;

    @JsonProperty("case_id")
    private String caseDataId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("case_role")
    private String caseRole;
}
