package uk.gov.hmcts.reform.fpl.model.notify.hearing;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

import java.util.Map;

@Getter
@Setter
@Builder
public final class NewNoticeOfHearingTemplate implements NotifyData {
    private String hearingType;
    private String familyManCaseNumber;
    private String respondentLastName;
    private String hearingDate;
    private String hearingVenue;
    private String preHearingTime;
    private String hearingTime;
    private String caseUrl;
    private Map<String, Object> documentLink;
    private String digitalPreference;
}
