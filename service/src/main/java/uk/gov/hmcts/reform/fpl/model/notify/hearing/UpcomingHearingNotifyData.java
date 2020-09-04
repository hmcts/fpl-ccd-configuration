package uk.gov.hmcts.reform.fpl.model.notify.hearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Builder
@EqualsAndHashCode
public class UpcomingHearingNotifyData implements NotifyData {
    @JsonProperty("hearing_date")
    private final String hearingDate;
    private final String cases;
}
