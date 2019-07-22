package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Hearing {

    private final String id;
    private final String description;
    private final String reason;
    private final String timeFrame;
    private final String sameDayHearingReason;
    private final String twoDayHearingReason;
    private final String sevenDayHearingReason;
    private final String twelveDayHearingReason;
    private final String withoutNotice;
    private final String reasonForNoNotice;
    private final String reducedNotice;
    private final String reasonForReducedNotice;
    private final String respondentsAware;
    private final String reasonsForRespondentsNotBeingAware;
    private final String createdBy;
    private final String createdDate;
    private final String updatedBy;
    private final String updatedOn;

}

