package uk.gov.hmcts.reform.am.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QueryResponse {

    private List<RoleAssignment> roleAssignmentResponse;

}
