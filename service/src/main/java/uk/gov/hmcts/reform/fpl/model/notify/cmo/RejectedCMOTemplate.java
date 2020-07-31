package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

@Getter
@Setter
public class RejectedCMOTemplate extends SharedNotifyTemplate {
    private String respondentLastName;
    private String familyManCaseNumber;
    private String hearing;
    private String requestedChanges;
}
