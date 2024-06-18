package uk.gov.hmcts.reform.am.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@NoArgsConstructor
@Jacksonized
@AllArgsConstructor
public class RoleAssignmentRequestResource {

    private AssignmentRequest roleAssignmentResponse;

}
