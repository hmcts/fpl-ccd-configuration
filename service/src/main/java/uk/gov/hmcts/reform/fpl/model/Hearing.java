package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingUrgencyType;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Hearing {
    private final HearingUrgencyType hearingUrgencyType;
    private final String hearingUrgencyDetails;
    private final YesNo withoutNotice;
    private final String withoutNoticeReason;
    private final YesNo respondentsAware;
    private final String respondentsAwareReason;

    @Deprecated(since = "DFPL-2304")
    private final String type;
    @Deprecated(since = "DFPL-2304")
    private final String reason;
    @Deprecated(since = "DFPL-2304")
    private final String timeFrame;
    @Deprecated(since = "DFPL-2304")
    private final String reducedNotice;
    @Deprecated(since = "DFPL-2304")
    private final String typeGiveReason;
    @Deprecated(since = "DFPL-2304")
    private final String reducedNoticeReason;
}
