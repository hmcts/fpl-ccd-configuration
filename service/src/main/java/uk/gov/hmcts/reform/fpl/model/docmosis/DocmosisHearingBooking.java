package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisHearingBooking {
    private final String hearingDate;
    private final String hearingVenue;
    private final String preHearingAttendance;
    private final String hearingTime;
    private final String hearingJudgeTitleAndName;
    private final String hearingLegalAdvisorName;
}
