package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum MandatoryDocuments {
    SOCIAL_WORK_CHRONOLOGY("socialWorkChronologyDocument", 1),
    SOCIAL_WORK_STATEMENT("socialWorkStatementDocument", 2),
    SOCIAL_WORK_ASSESSMENT("socialWorkAssessmentDocument", 3),
    SOCIAL_WORK_CARE_PLAN("socialWorkCarePlanDocument", 4),
    SOCIAL_WORK_EVIDENCE_TEMPLATE("socialWorkEvidenceTemplateDocument", 5),
    THRESHOLD("thresholdDocument", 6),
    CHECKLIST("checklistDocument", 7);

    private final String propertyKey;
    private final Integer interfaceDisplayOrder;

    MandatoryDocuments(String propertyKey, Integer interfaceDisplayOrder) {
        this.propertyKey = propertyKey;
        this.interfaceDisplayOrder = interfaceDisplayOrder;
    }
}

