package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.HearingHousekeepReason;
import uk.gov.hmcts.reform.fpl.enums.YesNo;

@Data
@Builder
public class ManageHearingHousekeepEventData {
    private YesNo hearingHousekeepOptions;
    private HearingHousekeepReason hearingHousekeepReason;
    private String hearingHousekeepReasonOther;

}
