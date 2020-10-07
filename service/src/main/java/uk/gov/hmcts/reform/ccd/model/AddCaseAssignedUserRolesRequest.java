package uk.gov.hmcts.reform.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component()
public class AddCaseAssignedUserRolesRequest {

    @JsonProperty("case_users")
    private List<CaseAssignedUserRoleWithOrganisation> caseAssignedUserRoles;
}
