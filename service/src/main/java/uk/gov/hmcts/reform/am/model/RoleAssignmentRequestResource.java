package uk.gov.hmcts.reform.am.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleAssignmentRequestResource {

    private AssignmentRequest roleAssignmentResponse;

}
