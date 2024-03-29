package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisHearingBooking {
    private final String hearingType;
    private final String hearingDate;
    private final String hearingVenue;
    private final String hearingAttendance;
    private final String hearingAttendanceDetails;
    private final String preHearingAttendance;
    private final String hearingTime;
    private final String hearingDuration;
    private final String hearingJudgeTitleAndName;
    private final String hearingLegalAdvisorName;
    private final String hearingStartDate;
    private final String hearingEndDate;
    private final String endDateDerived;
}
