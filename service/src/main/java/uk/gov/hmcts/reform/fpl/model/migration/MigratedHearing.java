package uk.gov.hmcts.reform.fpl.model.migration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonCreator
    public MigratedHearing(@JsonProperty("hearingDescription") final String hearingDescription,
                            @JsonProperty("reason") final String reason,
                            @JsonProperty("timeFrame") final String timeFrame,
                            @JsonProperty("sameDayHearingReason") final String sameDayHearingReason,
                            @JsonProperty("withoutNotice") final String withoutNotice,
                            @JsonProperty("reasonForNoNotice") final String reasonForNoNotice,
                            @JsonProperty("reducedNotice") final String reducedNotice,
                            @JsonProperty("reasonForReducedNotice") final String reasonForReducedNotice,
                            @JsonProperty("respondentsAware") final String respondentsAware,
                            @JsonProperty("reasonsForRespondentsNotBeingAware")
                               final String reasonsForRespondentsNotBeingAware) {
        this.hearingDescription = hearingDescription;
        this.reason = reason;
        this.timeFrame = timeFrame;
        this.sameDayHearingReason = sameDayHearingReason;
        this.withoutNotice = withoutNotice;
        this.reasonForNoNotice = reasonForNoNotice;
        this.reducedNotice = reducedNotice;
        this.reasonForReducedNotice = reasonForReducedNotice;
        this.respondentsAware = respondentsAware;
        this.reasonsForRespondentsNotBeingAware = reasonsForRespondentsNotBeingAware;
    }

}
