package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingUrgencyType;


@Data
@Jacksonized
@Builder(toBuilder = true)
public class Hearing {
    private final HearingUrgencyType hearingUrgencyType;
    private final String hearingUrgencyDetails;
    private final String withoutNotice;
    private final String withoutNoticeReason;
    private final String respondentsAware;
    private final String respondentsAwareReason;

    /**
     * @deprecated (DFPL-2304, historical hearing urgency field)
     */
    @Deprecated
    private final String type;
    /**
     * @deprecated (DFPL-2304, historical hearing urgency field)
     */
    @Deprecated
    private final String reason;
    /**
     * @deprecated (DFPL-2304, historical hearing urgency field)
     */
    @Deprecated
    private final String timeFrame;
    /**
     * @deprecated (DFPL-2304, historical hearing urgency field)
     */
    @Deprecated
    private final String reducedNotice;
    /**
     * @deprecated (DFPL-2304, historical hearing urgency field)
     */
    @Deprecated
    private final String typeGiveReason;
    /**
     * @deprecated (DFPL-2304, historical hearing urgency field)
     */
    @Deprecated
    private final String reducedNoticeReason;
}
