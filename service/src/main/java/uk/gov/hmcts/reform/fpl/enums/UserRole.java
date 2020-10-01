package uk.gov.hmcts.reform.fpl.enums;

import com.google.common.collect.ImmutableList;

import java.util.List;

public enum UserRole {
    LOCAL_AUTHORITY("caseworker-publiclaw-solicitor"),
    HMCTS_ADMIN("caseworker-publiclaw-courtadmin"),
    CAFCASS("caseworker-publiclaw-cafcass"),
    GATEKEEPER("caseworker-publiclaw-gatekeeper"),
    JUDICIARY("caseworker-publiclaw-judiciary"),
    HMCTS_SUPERUSER("caseworker-publiclaw-superuser");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public List<String> getRoles() {
        return ImmutableList.of("caseworker", "caseworker-publiclaw", this.role);
    }

    public static boolean isHmctsUser(String userRole) {
        List<UserRole> hmctsRoles = ImmutableList.of(HMCTS_ADMIN, GATEKEEPER, JUDICIARY, HMCTS_SUPERUSER);

        return hmctsRoles.stream().anyMatch(user -> user.role.equals(userRole));
    }
}
