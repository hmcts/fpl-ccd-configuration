package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;

@RequiredArgsConstructor
@Getter
public enum ApplicationDocumentType {

    THRESHOLD("Threshold", "Threshold"),
    SWET("SWET", "SWET"),
    CARE_PLAN("Care plan", "Care plan"),
    SOCIAL_WORK_CHRONOLOGY("Social work chronology", "Social work chronology"),
    SOCIAL_WORK_STATEMENT("Social work statement", "Social work statement"),
    GENOGRAM("Genogram", "Genogram"),
    CHECKLIST_DOCUMENT("Checklist document", "Checklist document"),
    BIRTH_CERTIFICATE("Birth certificate", "Birth certificate"),
    OTHER("Other", "Other");

    private final String label;
    private final String welshLabel;

    public String getLabel(Language language) {
        return language == Language.WELSH ? welshLabel : label;
    }

}
