package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ApplicationDocumentType {

    THRESHOLD("Threshold"),
    SWET("SWET"),
    CARE_PLAN("Care plan"),
    SOCIAL_WORK_CHRONOLOGY("Social work chronology"),
    SOCIAL_WORK_STATEMENT("Social work statement"),
    GENOGRAM("Genogram"),
    CHECKLIST_DOCUMENT("Checklist document"),
    BIRTH_CERTIFICATE("Birth certificate"),
    OTHER("Other");

    private final String label;
}
