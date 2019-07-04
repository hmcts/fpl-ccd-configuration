package uk.gov.hmcts.reform.fpl.model.migration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MigratedHearing {

    private final String hearingDescription;
    private final String reason;
    private final String timeFrame;
    private final String sameDayHearingReason;
    private final String withoutNotice;
    private final String reasonForNoNotice;
    private final String reducedNotice;
    private final String reasonForReducedNotice;
    private final String respondentsAware;
    private final String reasonsForRespondentsNotBeingAware;

}
