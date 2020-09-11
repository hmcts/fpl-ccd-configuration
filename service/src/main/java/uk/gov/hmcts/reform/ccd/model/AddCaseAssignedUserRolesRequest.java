package uk.gov.hmcts.reform.ccd.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCaseAssignedUserRolesRequest {
    private List<CaseAssignedUserRoleWithOrganisation> caseAssignedUserRoles;
}
