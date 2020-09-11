package uk.gov.hmcts.reform.ccd.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseAssignedUserRoleWithOrganisation {
    private String organisationId;
    private String caseDataId;
    private String userId;
    private String caseRole;
}
