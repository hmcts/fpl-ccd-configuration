package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Future;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.HearingNeedsBooked;
import uk.gov.hmcts.reform.fpl.enums.HearingStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.ModifiedOrderType;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingPresence;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingBookingDetailsGroup;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.HasEndDateAfterStartDate;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeNotMidnight;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.reform.fpl.config.TimeConfiguration.LONDON_TIMEZONE;
import static uk.gov.hmcts.reform.fpl.enums.HearingNeedsBooked.NONE;
import static uk.gov.hmcts.reform.fpl.enums.HearingNeedsBooked.SOMETHING_ELSE;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED_AND_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED_TO_BE_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED_AND_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED_TO_BE_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.IN_PERSON;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.PHONE;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.VIDEO;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.DEFAULT_PRE_ATTENDANCE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Data
@Builder(toBuilder = true)
@Jacksonized
@HasEndDateAfterStartDate(groups = HearingBookingDetailsGroup.class)
public class HearingBooking implements TranslatableItem {
    private HearingType type;
    private HearingStatus status;
    private final String typeDetails;
    private final String typeReason;
    private final String venue;
    private final String customPreviousVenue;
    private final Address venueCustomAddress;
    private final HearingPresence presence;
    private final List<HearingAttendance> attendance;
    private final String attendanceDetails;
    private final String preAttendanceDetails;
    @TimeNotMidnight(message = "Enter a valid start time", groups = HearingBookingDetailsGroup.class)
    @Future(message = "Enter a start date in the future", groups = HearingBookingDetailsGroup.class)
    private final LocalDateTime startDate;
    @TimeNotMidnight(message = "Enter a valid end time", groups = HearingBookingDetailsGroup.class)
    @Future(message = "Enter an end date in the future", groups = HearingBookingDetailsGroup.class)
    private final LocalDateTime endDate;
    private final LocalDate vacatedDate;
    private final String hearingDuration;
    private final String endDateDerived;
    private final Integer hearingDays;
    private final Integer hearingMinutes;
    private final Integer hearingHours;
    private final List<HearingNeedsBooked> hearingNeedsBooked;
    private final String hearingNeedsDetails;
    private final String additionalNotes;
    private final String allocatedJudgeLabel;
    private final String hearingJudgeLabel;
    private final String legalAdvisorLabel;
    //judgeAndLegalAdvisor field not shown in tab for new hearings but shown for hearings before FPLA-2030
    private JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private final List<Element<Other>> others;
    private final String othersNotified;
    private UUID caseManagementOrderId;
    private DocumentReference noticeOfHearing;
    private final DocumentReference translatedNoticeOfHearing;
    private final LocalDateTime translationUploadDateTime;
    private LanguageTranslationRequirement translationRequirements;
    private final PreviousHearingVenue previousHearingVenue;
    private String cancellationReason;
    private String housekeepReason;
    private DocumentReference noticeOfHearingVacated;

    public boolean hasDatesOnSameDay() {
        return this.startDate.toLocalDate().isEqual(this.endDate.toLocalDate());
    }

    public LocalDateTime getEndDate() {
        LocalDateTime date = this.startDate;
        Integer hearingDays = nonNull(this.hearingDays) ? this.hearingDays : null;
        int counter = 1;

        if (isNull(date) || isNull(hearingDays)) {
            return this.endDate;
        }

        while (counter < hearingDays) {
            date = date.plusDays(1);

            if (DayOfWeek.SATURDAY.equals(date.getDayOfWeek())
                || DayOfWeek.SUNDAY.equals(date.getDayOfWeek())) {

                continue;
            }

            counter++;
        }

        return date;
    }

    public boolean startsAfterToday() {
        return ofNullable(startDate)
            .map(date -> date.isAfter(ZonedDateTime.now(LONDON_TIMEZONE).toLocalDateTime()))
            .orElse(false);
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
        String hearingLabel = ofNullable(this.type)
            .map(HearingType::getLabel)
            .orElse("Other");
        String label =
            format("%s hearing, %s", hearingLabel, formatLocalDateTimeBaseUsingFormat(startDate, DATE));
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

    public List<HearingAttendance> getAttendance() {
        if (isEmpty(attendance)) {
            if (presence == HearingPresence.REMOTE) {
                return List.of(VIDEO);
            }
            if (presence == HearingPresence.IN_PERSON) {
                return List.of(IN_PERSON);
            }
        }
        return attendance;
    }

    public String getPreAttendanceDetails() {
        return defaultIfEmpty(preAttendanceDetails, DEFAULT_PRE_ATTENDANCE);
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
        return isNotEmpty(getAttendance()) && (getAttendance().contains(VIDEO) || getAttendance().contains(PHONE));
    }

    @Override
    public LanguageTranslationRequirement getTranslationRequirements() {
        return defaultIfNull(translationRequirements, NO);
    }

    @Override
    public LocalDateTime translationUploadDateTime() {
        return translationUploadDateTime;
    }

    @Override
    @JsonIgnore
    public boolean hasBeenTranslated() {
        return nonNull(translatedNoticeOfHearing);
    }

    @Override
    @JsonIgnore
    public DocumentReference getTranslatedDocument() {
        return translatedNoticeOfHearing;
    }

    @Override
    @JsonIgnore
    public DocumentReference getDocument() {
        return noticeOfHearing;
    }

    @Override
    @JsonIgnore
    public String asLabel() {
        return format("Notice of hearing - %s", formatLocalDateTimeBaseUsingFormat(startDate, DATE));
    }

    @Override
    @JsonIgnore
    public String getModifiedItemType() {
        return ModifiedOrderType.NOTICE_OF_HEARING.getLabel();
    }

    @Override
    @JsonIgnore
    public List<Element<Other>> getSelectedOthers() {
        return defaultIfNull(this.getOthers(), new ArrayList<>());
    }
}
