package uk.gov.hmcts.reform.fpl.enums;

import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Respondent;

import java.util.Arrays;

public enum ConfidentialPartyType {
    RESPONDENT(Respondent.class, "confidentialRespondents"),
    CHILD(Child.class, "confidentialChildren");

    private final Class type;
    private final String caseDataKey;

    ConfidentialPartyType(Class type, String caseDataKey) {
        this.type = type;
        this.caseDataKey = caseDataKey;
    }

    public static String getCaseDataKeyFromClass(Class partyType) {
        return Arrays.stream(values())
            .filter(value -> value.type == partyType)
            .map(value -> value.caseDataKey)
            .findFirst()
            .orElseThrow();
    }
}
