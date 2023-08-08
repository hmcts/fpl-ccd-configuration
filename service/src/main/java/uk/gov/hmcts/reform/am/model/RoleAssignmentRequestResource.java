package uk.gov.hmcts.reform.am.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentRequestResource {

    private AssignmentRequest roleAssignmentResponse;

}
