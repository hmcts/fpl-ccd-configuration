package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Builder(toBuilder = true)
public class Hearing {
    private final String hearingID;
    private final String hearingDescription;
    private final String hearingDate;
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

    @JsonCreator
    public Hearing(@JsonProperty("hearingID") final String hearingID,
                   @JsonProperty("hearingDescription") final String hearingDescription,
                   @JsonProperty("hearingDate") final String hearingDate,
                   @JsonProperty("reason") final String reason,
                   @JsonProperty("timeFrame") final String timeFrame,
                   @JsonProperty("sameDayHearingReason") final String sameDayHearingReason,
                   @JsonProperty("twoDayHearingReason") final String twoDayHearingReason,
                   @JsonProperty("sevenDayHearingReason") final String sevenDayHearingReason,
                   @JsonProperty("twelveDayHearingReason") final String twelveDayHearingReason,
                   @JsonProperty("withoutNotice") final String withoutNotice,
                   @JsonProperty("reasonForNoNotice") final String reasonForNoNotice,
                   @JsonProperty("reducedNotice") final String reducedNotice,
                   @JsonProperty("reasonForReducedNotice") final String reasonForReducedNotice,
                   @JsonProperty("respondentsAware") final String respondentsAware,
                   @JsonProperty("reasonsForRespondentsNotBeingAware") final String reasonsForRespondentsNotBeingAware,
                   @JsonProperty("createdBy") final String createdBy,
                   @JsonProperty("createdDate") final String createdDate,
                   @JsonProperty("updatedBy") final String updatedBy,
                   @JsonProperty("updatedOn") final String updatedOn) {
        this.hearingID = hearingID;
        this.hearingDescription = hearingDescription;
        this.hearingDate = hearingDate;
        this.reason = reason;
        this.timeFrame = timeFrame;
        this.sameDayHearingReason = sameDayHearingReason;
        this.twoDayHearingReason = twoDayHearingReason;
        this.sevenDayHearingReason = sevenDayHearingReason;
        this.twelveDayHearingReason = twelveDayHearingReason;
        this.withoutNotice = withoutNotice;
        this.reasonForNoNotice = reasonForNoNotice;
        this.reducedNotice = reducedNotice;
        this.reasonForReducedNotice = reasonForReducedNotice;
        this.respondentsAware = respondentsAware;
        this.reasonsForRespondentsNotBeingAware = reasonsForRespondentsNotBeingAware;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.updatedBy = updatedBy;
        this.updatedOn = updatedOn;
    }

}

