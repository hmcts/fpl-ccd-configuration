package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingBookingDetailsGroup;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.HasEndDateAfterStartDate;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeNotMidnight;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import javax.validation.constraints.Future;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.OTHER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@HasEndDateAfterStartDate(groups = HearingBookingDetailsGroup.class)
public class HearingBooking {
    private final HearingType type;
    private final String typeDetails;
    private final String venue;
    private final Address venueCustomAddress;
    @TimeNotMidnight(message = "Enter a valid start time", groups = HearingBookingDetailsGroup.class)
    @Future(message = "Enter a start date in the future", groups = HearingBookingDetailsGroup.class)
    private final LocalDateTime startDate;
    @TimeNotMidnight(message = "Enter a valid end time", groups = HearingBookingDetailsGroup.class)
    @Future(message = "Enter an end date in the future", groups = HearingBookingDetailsGroup.class)
    private final LocalDateTime endDate;
    private final List<String> hearingNeedsBooked;
    private final String hearingNeedsDetails;
    private JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private UUID caseManagementOrderId;
    private DocumentReference noticeOfHearing;

    public boolean hasDatesOnSameDay() {
        return this.startDate.toLocalDate().isEqual(this.endDate.toLocalDate());
    }

    public boolean startsAfterToday() {
        return startDate.isAfter(ZonedDateTime.now(ZoneId.of("Europe/London")).toLocalDateTime());
    }

    public boolean hasCMOAssociation() {
        return caseManagementOrderId != null;
    }

    public String toLabel(String dateFormat) {
        String label = OTHER == type ? capitalize(typeDetails) : type.getLabel();
        return format("%s hearing, %s", label, formatLocalDateTimeBaseUsingFormat(startDate, dateFormat));
    }
}
