package uk.gov.hmcts.reform.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CaseAssignedUserRolesRequest {

    @JsonProperty("case_users")
    private List<CaseAssignedUserRoleWithOrganisation> caseAssignedUserRoles;

}
