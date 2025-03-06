package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;

@RequiredArgsConstructor
@Getter
public enum ApplicationDocumentType {

    THRESHOLD("Threshold", "Trothwy"),
    SWET("SWET", "SWET"),
    CARE_PLAN("Care plan", "Cynllun gofal"),
    SOCIAL_WORK_CHRONOLOGY("Social work chronology", "Cronoleg gwaith cymdeithasol"),
    SOCIAL_WORK_STATEMENT("Social work statement", "Datganiad gwaith cymdeithasol"),
    GENOGRAM("Genogram", "Genogram"),
    CHECKLIST_DOCUMENT("Checklist document", "Dogfen wirio"),
    BIRTH_CERTIFICATE("Birth certificate", "Tystysgrif geni"),
    STATEMENT("Statement", "Datganiad"),
    OTHER("Other", "Arall");

    private final String label;
    private final String welshLabel;

    public String getLabel(Language language) {
        return language == Language.WELSH ? welshLabel : label;
    }

}
