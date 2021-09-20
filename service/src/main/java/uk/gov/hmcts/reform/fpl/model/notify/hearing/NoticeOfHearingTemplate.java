package uk.gov.hmcts.reform.fpl.model.notify.hearing;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
public final class NoticeOfHearingTemplate extends BaseCaseNotifyData {
    private String hearingType;
    private String familyManCaseNumber;
    private String hearingDate;
    private String hearingVenue;
    private String preHearingTime;
    private String hearingTime;
    private Object documentLink;
    private String digitalPreference;
    private String childLastName;
}
