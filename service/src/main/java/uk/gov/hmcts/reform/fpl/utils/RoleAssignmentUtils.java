package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.am.model.GrantType;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.am.model.RoleCategory;
import uk.gov.hmcts.reform.am.model.RoleType;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

public class RoleAssignmentUtils {

    private RoleAssignmentUtils() {

    }

    public static RoleAssignment buildRoleAssignment(Long caseId, String userId, String role,
                                              RoleCategory roleCategory, ZonedDateTime beginTime,
                                              ZonedDateTime endTime) {
        return RoleAssignment.builder()
            .actorId(userId)
            .attributes(Map.of("caseId", caseId.toString(),
                "caseType", CASE_TYPE,
                "jurisdiction", JURISDICTION,
                "substantive", "Y"))
            .grantType(GrantType.SPECIFIC)
            .roleCategory(roleCategory)
            .roleType(RoleType.CASE)
            // todo may need to change this after COT change
            .classification(roleCategory.equals(RoleCategory.LEGAL_OPERATIONS) ? "RESTRICTED" : "PUBLIC")
            .beginTime(beginTime)
            .endTime(endTime)
            .roleName(role)
            .readOnly(false)
            .build();
    }

    public static List<RoleAssignment> buildRoleAssignments(Long caseId, List<String> userIds, String role,
                                                     RoleCategory roleCategory, ZonedDateTime beginTime,
                                                     ZonedDateTime endTime) {
        return userIds.stream()
            .map(user -> buildRoleAssignment(caseId, user, role, roleCategory, beginTime, endTime))
            .collect(Collectors.toList());
    }

}
