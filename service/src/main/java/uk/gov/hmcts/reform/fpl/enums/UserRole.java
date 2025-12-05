package uk.gov.hmcts.reform.fpl.enums;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Set;

public enum UserRole {
    LOCAL_AUTHORITY("caseworker-publiclaw-solicitor"),
    HMCTS_ADMIN("caseworker-publiclaw-courtadmin"),
    CAFCASS("caseworker-publiclaw-cafcass"),
    CAFCASS_SYSTEM_UPDATE("caseworker-publiclaw-cafcasssystemupdate"),
    ROBOTICS("caseworker-publiclaw-rparobot"),
    GATEKEEPER("caseworker-publiclaw-gatekeeper"),
    JUDICIARY("caseworker-publiclaw-judiciary"),
    HMCTS_SUPERUSER("caseworker-publiclaw-superuser"),
    FPL_SYSTEM_UPDATE("caseworker-publiclaw-systemupdate");

    private final String roleName;

    UserRole(String roleName) {
        this.roleName = roleName;
    }

    public List<String> getRoleNames() {
        return ImmutableList.of("caseworker", "caseworker-publiclaw", this.roleName);
    }

    public static boolean isHmctsUser(String userRole) {
        List<UserRole> hmctsRoles = ImmutableList.of(HMCTS_ADMIN, GATEKEEPER, JUDICIARY, HMCTS_SUPERUSER);

        return hmctsRoles.stream().anyMatch(user -> user.roleName.equals(userRole));
    }

    public String getRoleName() {
        return roleName;
    }

    public static boolean isSystemUser(String userRole) {
        return Set.of(CAFCASS_SYSTEM_UPDATE.roleName, FPL_SYSTEM_UPDATE.roleName).contains(userRole);
    }
}
