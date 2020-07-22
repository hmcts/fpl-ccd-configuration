package uk.gov.hmcts.reform.fpl.model.notify.draftcmo;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

@Getter
@Setter
public final class IssuedCMOTemplate extends SharedNotifyTemplate {
    private String respondentLastName;
    private String familyManCaseNumber;
    private String hearingDate;
    private String digitalPreference;
}
