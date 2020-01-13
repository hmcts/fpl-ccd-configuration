package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

public enum ConfidentialPartyType {
    RESPONDENT("confidentialRespondents"),
    CHILD("confidentialChildren");

    @Getter
    private final String caseDataKey;

    ConfidentialPartyType(String caseDataKey) {
        this.caseDataKey = caseDataKey;
    }
}
