package uk.gov.hmcts.reform.fpl.enums;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.GATEKEEPER;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.HMCTS_ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;

public class UserRolesTest {

    @Test
    public void shouldProduceAllIdamRoles() {

        List<String> standardRoles = List.of("caseworker", "caseworker-publiclaw");

        assertThat(CAFCASS.getRoles()).containsAll(standardRoles).contains("caseworker-publiclaw-cafcass");
        assertThat(GATEKEEPER.getRoles()).containsAll(standardRoles).contains("caseworker-publiclaw-gatekeeper");
        assertThat(HMCTS_ADMIN.getRoles()).containsAll(standardRoles).contains("caseworker-publiclaw-courtadmin");
        assertThat(JUDICIARY.getRoles()).containsAll(standardRoles).contains("caseworker-publiclaw-judiciary");
        assertThat(LOCAL_AUTHORITY.getRoles()).containsAll(standardRoles).contains("caseworker-publiclaw-solicitor");
    }
}
