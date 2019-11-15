package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.interfaces.HearingBookingDetailsGroup;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.HasFutureDate;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.HasStartDateAfterEndDate;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeNotZero;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@HasStartDateAfterEndDate(groups = HearingBookingDetailsGroup.class)
public class HearingBooking {
    private final String type;
    private final String typeDetails;
    private final String venue;
    @TimeNotZero(message = "Enter a valid start time", groups = HearingBookingDetailsGroup.class)
    @HasFutureDate(message = "Enter a start date in the future", groups = HearingBookingDetailsGroup.class)
    private final LocalDateTime startDate;
    @TimeNotZero(message = "Enter a valid end time", groups = HearingBookingDetailsGroup.class)
    @HasFutureDate(message = "Enter an end date in the future", groups = HearingBookingDetailsGroup.class)
    private final LocalDateTime endDate;
    private final List<String> hearingNeedsBooked;
    private final String hearingNeedsDetails;
    private final String judgeTitle;
    private final String judgeName;

    public boolean onSameDay() {
        return this.startDate.toLocalDate().isEqual(this.endDate.toLocalDate());
    }
}
