package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

public enum ConfidentialPartyType {
    RESPONDENT("confidentialRespondents", "respondents1"),
    CHILD("confidentialChildren", "children1"),
    OTHER("confidentialOthers", "othersV2");

    @Getter
    private final String confidentialKey;

    @Getter
    private final String caseDataKey;

    ConfidentialPartyType(String confidentialKey, String caseDataKey) {
        this.confidentialKey = confidentialKey;
        this.caseDataKey = caseDataKey;
    }
}
