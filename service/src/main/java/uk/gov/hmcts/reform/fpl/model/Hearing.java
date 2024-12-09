package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingUrgencyType;
import uk.gov.hmcts.reform.fpl.json.deserializer.YesNoDeserializer;


@Data
@Jacksonized
@Builder(toBuilder = true)
public class Hearing {
    private final HearingUrgencyType hearingUrgencyType;
    private final String hearingUrgencyDetails;
    @JsonDeserialize(using = YesNoDeserializer.class)
    private final YesNo withoutNotice;
    private final String withoutNoticeReason;
    @JsonDeserialize(using = YesNoDeserializer.class)
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
