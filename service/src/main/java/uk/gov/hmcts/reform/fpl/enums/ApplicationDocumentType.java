package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@Getter
public enum ApplicationDocumentType {

    @CCD(label = "Threshold")
    THRESHOLD("Threshold", "Trothwy"),
    SWET("SWET", "SWET"),
    @CCD(label = "Care plan")
    CARE_PLAN("Care plan", "Cynllun gofal"),
    @CCD(label = "Social work chronology")
    SOCIAL_WORK_CHRONOLOGY("Social work chronology", "Cronoleg gwaith cymdeithasol"),
    @CCD(label = "Social work statement")
    SOCIAL_WORK_STATEMENT("Social work statement", "Datganiad gwaith cymdeithasol"),
    @CCD(label = "Genogram")
    GENOGRAM("Genogram", "Genogram"),
    @CCD(label = "Checklist document")
    CHECKLIST_DOCUMENT("Checklist document", "Dogfen wirio"),
    @CCD(label = "Birth certificate")
    BIRTH_CERTIFICATE("Birth certificate", "Tystysgrif geni"),
    @CCD(label = "Statement")
    STATEMENT("Statement", "Datganiad"),
    @CCD(label = "Other")
    OTHER("Other", "Arall");

    private final String label;
    private final String welshLabel;

    public String getLabel(Language language) {
        return language == Language.WELSH ? welshLabel : label;
    }

}
