package uk.gov.hmcts.reform.fpl.model.notify.hearing;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
public class HearingVacatedTemplate extends BaseCaseNotifyData {
    private LocalDateTime hearingDate;
    private String hearingDateFormatted;
    private String hearingVenue;
    private String hearingTime;
    private String vacatedDate;
    private String vacatedReason;
    private String relistAction;
    private String familyManCaseNumber;
}
