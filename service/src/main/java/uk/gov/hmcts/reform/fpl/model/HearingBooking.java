package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class HearingBooking {
    private final String type;
    private final String typeDetails;
    private final String venue;
    private final LocalDate date;
    private final String preHearingAttendance;
    private final String time;
    private final List<String> hearingNeedsBooked;
    private final String hearingNeedsDetails;
    private final String judgeTitle;
    private final String judgeName;
    private final DynamicList venueList;

    // TODO: 15/10/2019 When actual integration occurs and we remove venue for the dynamic list
    //   we should create a getVenue method that returns the label of the selected value in the dynamic list
    //   Not implemented yet as this would mess with current design
}
