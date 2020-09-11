package uk.gov.hmcts.reform.ccd.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCaseAssignedUserRolesResponse {
    private String status;
}
