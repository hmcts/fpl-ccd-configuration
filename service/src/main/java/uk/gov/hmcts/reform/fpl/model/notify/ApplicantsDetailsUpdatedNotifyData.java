package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class ApplicantsDetailsUpdatedNotifyData implements NotifyData {
    private final String firstRespondentLastNameOrLaName;
    private final String familyManCaseNumber;
    private final String caseUrl;
}
