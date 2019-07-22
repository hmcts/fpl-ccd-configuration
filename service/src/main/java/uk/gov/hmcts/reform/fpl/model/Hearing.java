package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class Hearing {
    private final String hearingID;
    private final String hearingDescription;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final Date hearingDate;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final Date createdDate;
    private final String updatedBy;
    private final String updatedOn;
}

