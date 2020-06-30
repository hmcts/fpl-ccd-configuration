package uk.gov.hmcts.reform.fpl.model.notify.hearing;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Getter
@Setter
@Builder
public final class NewNoticeOfHearingTemplate implements NotifyData {
    private String familyManCaseNumber;
    private String respondentLastName;
    private String localAuthority;
    private String hearingDate;
    private String caseUrl;
}
