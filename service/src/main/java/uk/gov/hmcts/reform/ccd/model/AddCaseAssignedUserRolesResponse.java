package uk.gov.hmcts.reform.ccd.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class AddCaseAssignedUserRolesResponse {
    private String status;
}
