package uk.gov.hmcts.reform.fpl.enums;

import com.google.common.collect.ImmutableList;

import java.util.List;

public enum UserRole {
    LOCAL_AUTHORITY("caseworker-publiclaw-solicitor"),
    HMCTS_ADMIN("caseworker-publiclaw-courtadmin"),
    CAFCASS("caseworker-publiclaw-cafcass"),
    GATEKEEPER("caseworker-publiclaw-gatekeeper"),
    JUDICIARY("caseworker-publiclaw-judiciary");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public List<String> getRoles() {
        return ImmutableList.of("caseworker", "caseworker-publiclaw", this.role);
    }
}
