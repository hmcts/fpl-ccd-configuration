package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.interfaces.HearingBookingDetailsGroup;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasFutureDate;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasStartDateAfterEndDate;
import uk.gov.hmcts.reform.fpl.validators.interfaces.TimeNotZero;

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
    @TimeNotZero(groups = HearingBookingDetailsGroup.class)
    @HasFutureDate(message = "Enter a start date in the future", groups = HearingBookingDetailsGroup.class)
    private final LocalDateTime startDate;
    @TimeNotZero(groups = HearingBookingDetailsGroup.class)
    @HasFutureDate(message = "Enter an end date in the future", groups = HearingBookingDetailsGroup.class)
    private final LocalDateTime endDate;
    private final List<String> hearingNeedsBooked;
    private final String hearingNeedsDetails;
    private final String judgeTitle;
    private final String judgeName;
}
