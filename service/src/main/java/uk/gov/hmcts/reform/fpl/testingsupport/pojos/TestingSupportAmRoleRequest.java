package uk.gov.hmcts.reform.fpl.testingsupport.pojos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.am.model.RoleCategory;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TestingSupportAmRoleRequest {
    private List<String> userIds;
    private String role;
    private RoleCategory roleCategory;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
}
