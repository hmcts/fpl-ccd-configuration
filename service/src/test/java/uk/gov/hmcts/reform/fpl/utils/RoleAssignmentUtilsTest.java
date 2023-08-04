package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.am.model.GrantType;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.am.model.RoleCategory;
import uk.gov.hmcts.reform.am.model.RoleType;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RoleAssignmentUtilsTest {

    @Nested
    class BuildAssignments {

        private final ZonedDateTime now = ZonedDateTime.now();

        @Test
        void shouldBuildRoleAssignmentWithGivenProperties() {
            RoleAssignment role = RoleAssignmentUtils.buildRoleAssignment(12345L, "userId", "role",
                RoleCategory.JUDICIAL, now, now);

            role.setCreated(now); // override the default behaviour, as now() is hard to test, millisecond delays!

            RoleAssignment expected = RoleAssignment.builder()
                .actorId("userId")
                .attributes(Map.of("caseId", "12345", "caseType", "CARE_SUPERVISION_EPO",
                    "jurisdiction", "PUBLICLAW", "substantive", "Y"))
                .grantType(GrantType.SPECIFIC)
                .roleCategory(RoleCategory.JUDICIAL)
                .roleType(RoleType.CASE)
                .beginTime(now)
                .endTime(now)
                .roleName("role")
                .readOnly(false)
                .actorIdType("IDAM")
                .created(now)
                .status("CREATE_REQUESTED")
                .classification("PUBLIC")
                .build();

            assertThat(role).isEqualTo(expected);

        }

    }

}
