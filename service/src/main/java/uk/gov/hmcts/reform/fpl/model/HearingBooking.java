package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.HearingNeedsBooked;
import uk.gov.hmcts.reform.fpl.enums.HearingStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingPresence;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingBookingDetailsGroup;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.HasEndDateAfterStartDate;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeNotMidnight;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.validation.constraints.Future;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static uk.gov.hmcts.reform.fpl.enums.HearingNeedsBooked.NONE;
import static uk.gov.hmcts.reform.fpl.enums.HearingNeedsBooked.SOMETHING_ELSE;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED_AND_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED_TO_BE_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED_AND_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED_TO_BE_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.OTHER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Data
@Builder(toBuilder = true)
@Jacksonized
@HasEndDateAfterStartDate(groups = HearingBookingDetailsGroup.class)
public class HearingBooking {
    private final HearingType type;
    private HearingStatus status;
    private final String typeDetails;
    private final String venue;
    private final String customPreviousVenue;
    private final Address venueCustomAddress;
    private final HearingPresence presence;
    @TimeNotMidnight(message = "Enter a valid start time", groups = HearingBookingDetailsGroup.class)
    @Future(message = "Enter a start date in the future", groups = HearingBookingDetailsGroup.class)
    private final LocalDateTime startDate;
    @TimeNotMidnight(message = "Enter a valid end time", groups = HearingBookingDetailsGroup.class)
    @Future(message = "Enter an end date in the future", groups = HearingBookingDetailsGroup.class)
    private final LocalDateTime endDate;
    private final List<HearingNeedsBooked> hearingNeedsBooked;
    private final String hearingNeedsDetails;
    private final String additionalNotes;
    private final String allocatedJudgeLabel;
    private final String hearingJudgeLabel;
    private final String legalAdvisorLabel;
    //judgeAndLegalAdvisor field not shown in tab for new hearings but shown for hearings before FPLA-2030
    private JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private UUID caseManagementOrderId;
    private DocumentReference noticeOfHearing;
    private final PreviousHearingVenue previousHearingVenue;
    private String cancellationReason;

    public boolean hasDatesOnSameDay() {
        return this.startDate.toLocalDate().isEqual(this.endDate.toLocalDate());
    }

    public boolean startsAfterToday() {
        return startDate.isAfter(ZonedDateTime.now(ZoneId.of("Europe/London")).toLocalDateTime());
    }

    public boolean startsTodayOrBefore() {
        return ofNullable(startDate)
            .map(date -> date.toLocalDate().isBefore(LocalDate.now().plusDays(1)))
            .orElse(false);
    }

    public boolean startsTodayOrAfter() {
        return ofNullable(startDate)
            .map(date -> date.toLocalDate().isAfter(LocalDate.now().minusDays(1)))
            .orElse(false);
    }

    public boolean hasCMOAssociation() {
        return caseManagementOrderId != null;
    }

    public String toLabel() {
        String type = OTHER == this.type ? capitalize(typeDetails) : this.type.getLabel();
        String label = format("%s hearing, %s", type, formatLocalDateTimeBaseUsingFormat(startDate, DATE));
        String status = isAdjourned() ? "adjourned" : isVacated() ? "vacated" : null;

        return ofNullable(status).map(suffix -> label + " - " + suffix).orElse(label);
    }

    public List<String> buildHearingNeedsList() {
        List<String> list = new ArrayList<>();

        if (hearingNeedsBooked != null && !hearingNeedsBooked.isEmpty()) {
            for (HearingNeedsBooked hearingNeed : hearingNeedsBooked) {
                if (hearingNeed == NONE) {
                    return emptyList();
                }
                if (hearingNeed != SOMETHING_ELSE) {
                    list.add(hearingNeed.getLabel());
                }
            }
        }
        return list;
    }

    @JsonIgnore
    public boolean isOfType(HearingType hearingType) {
        return type == hearingType;
    }

    @JsonIgnore
    public boolean isAdjourned() {
        return status == ADJOURNED || status == ADJOURNED_TO_BE_RE_LISTED || status == ADJOURNED_AND_RE_LISTED;
    }

    @JsonIgnore
    public boolean isVacated() {
        return status == VACATED || status == VACATED_TO_BE_RE_LISTED || status == VACATED_AND_RE_LISTED;
    }

    @JsonIgnore
    public boolean isToBeReListed() {
        return status == VACATED_TO_BE_RE_LISTED || status == ADJOURNED_TO_BE_RE_LISTED;
    }

    @JsonIgnore
    public boolean isRemote() {
        return HearingPresence.REMOTE == presence;
    }
}
