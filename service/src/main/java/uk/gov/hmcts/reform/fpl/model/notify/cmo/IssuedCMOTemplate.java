package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class IssuedCMOTemplate extends SharedNotifyTemplate {
    private final String respondentLastName;
    private final String familyManCaseNumber;
    private final String hearing;
    private final String digitalPreference;
}
