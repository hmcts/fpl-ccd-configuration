package uk.gov.hmcts.reform.fpl.enums;

import uk.gov.hmcts.ccd.sdk.types.HasRole;
import com.google.common.collect.ImmutableList;

import java.util.List;

public enum UserRole implements HasRole {
    LOCAL_AUTHORITY("caseworker-publiclaw-solicitor"),
    HMCTS_ADMIN("caseworker-publiclaw-courtadmin"),
    CAFCASS("caseworker-publiclaw-cafcass"),
    GATEKEEPER("caseworker-publiclaw-gatekeeper"),
    JUDICIARY("caseworker-publiclaw-judiciary"),
    SYSTEM_UPDATE("caseworker-publiclaw-systemupdate"),
    BULK_SCAN("caseworker-publiclaw-bulkscan"),
    BULK_SCAN_SYSTEM_UPDATE("caseworker-publiclaw-bulkscansystemupdate"),
    CCD_SOLICITOR("[SOLICITOR]"),
    CCD_LASOLICITOR("[LASOLICITOR]");


    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return this.role;
    }

    public List<String> getRoles() {
        return ImmutableList.of("caseworker", "caseworker-publiclaw", this.role);
    }
}
