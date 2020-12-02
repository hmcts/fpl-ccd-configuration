package uk.gov.hmcts.reform.fpl.model.notify.hearing;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public final class NoticeOfHearingTemplate extends BaseCaseNotifyData {
    private String hearingType;
    private String familyManCaseNumber;
    private String hearingDate;
    private String hearingVenue;
    private String preHearingTime;
    private String hearingTime;
    private Map<String, Object> documentLink;
    private String digitalPreference;
}
