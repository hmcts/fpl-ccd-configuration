package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingBookingDetailsGroup;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.HasFutureDate;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.HasSetTime;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.HasStartDateAfterEndDate;

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
    @HasSetTime(message = "Enter a valid start time", groups = HearingBookingDetailsGroup.class)
    @HasFutureDate(message = "Enter a start date in the future", groups = HearingBookingDetailsGroup.class)
    private final LocalDateTime startDate;
    @HasSetTime(message = "Enter a valid end time", groups = HearingBookingDetailsGroup.class)
    @HasFutureDate(message = "Enter an end date in the future", groups = HearingBookingDetailsGroup.class)
    private final LocalDateTime endDate;
    private final List<String> hearingNeedsBooked;
    private final String hearingNeedsDetails;
    private final String judgeTitle;
    private final String judgeName;

    public boolean hasDatesOnSameDay() {
        return this.startDate.toLocalDate().isEqual(this.endDate.toLocalDate());
    }
}
