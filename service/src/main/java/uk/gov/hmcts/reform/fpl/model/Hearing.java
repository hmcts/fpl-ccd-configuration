package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingUrgencyType;

import static java.util.Objects.nonNull;


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
     * This historical hearing urgency field is deprecated since DFPL-2304.
     * @deprecated (DFPL-2304, historical hearing urgency field)
     */
    @Deprecated(since = "DFPL-2304")
    private final String type;
    /**
     * This historical hearing urgency field is deprecated since DFPL-2304.
     * @deprecated (DFPL-2304, historical hearing urgency field)
     */
    @Deprecated(since = "DFPL-2304")
    private final String reason;
    /**
     * This historical hearing urgency field is deprecated since DFPL-2304.
     * @deprecated (DFPL-2304, historical hearing urgency field)
     */
    @Deprecated(since = "DFPL-2304")
    private final String timeFrame;
    /**
     * This historical hearing urgency field is deprecated since DFPL-2304.
     * @deprecated (DFPL-2304, historical hearing urgency field)
     */
    @Deprecated(since = "DFPL-2304")
    private final String reducedNotice;
    /**
     * This historical hearing urgency field is deprecated since DFPL-2304.
     * @deprecated (DFPL-2304, historical hearing urgency field)
     */
    @Deprecated(since = "DFPL-2304")
    private final String typeGiveReason;
    /**
     * This historical hearing urgency field is deprecated since DFPL-2304.
     * @deprecated (DFPL-2304, historical hearing urgency field)
     */
    @Deprecated(since = "DFPL-2304")
    private final String reducedNoticeReason;

    /**
     * Use this method if backward compatible with historical data if required.
     * @return hearingUrgencyType if not null, otherwise return timeFrame
     */
    @JsonIgnore
    @SuppressWarnings("java:S1874")
    public String getHearingUrgencyTypeOrTimeFrame() {
        return nonNull(hearingUrgencyType) ? hearingUrgencyType.getLabel() : timeFrame;
    }
}
