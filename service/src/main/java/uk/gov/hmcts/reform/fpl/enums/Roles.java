package uk.gov.hmcts.reform.fpl.enums;

public enum Roles {

    ADMIN("caseworker-publiclaw-courtadmin"),
    LA_SOLICITOR("caseworker-publiclaw-solicitor");

    final String name;

    Roles(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
