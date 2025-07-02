package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class UpdateGuardianNotifyData implements NotifyData {
    private final String firstRespondentLastName;
    private final String familyManCaseNumber;
    private final String caseUrl;
}
