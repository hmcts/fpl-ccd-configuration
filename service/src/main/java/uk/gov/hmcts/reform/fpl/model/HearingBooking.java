package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class HearingBooking {
    private final String hearingType;
    private final String hearingTypeDetails;
    private final String hearingVenue;
    private final LocalDate hearingDate;
    private final String preHearingAttendance;
    private final String hearingTime;
    private final List<String> hearingNeededDetails;
    private final String hearingNeededGiveDetails;
    private final String judgeTitle;
    private final String judgeFullName;
}
