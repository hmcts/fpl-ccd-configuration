package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

@SuperBuilder
public class RejectedCMOTemplate extends SharedNotifyTemplate {
    private final String respondentLastName;
    private final String familyManCaseNumber;
    private final String hearing;
    private final String requestedChanges;
}
