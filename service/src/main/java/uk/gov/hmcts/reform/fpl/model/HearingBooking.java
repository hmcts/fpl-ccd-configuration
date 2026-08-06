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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@Jacksonized
@HasEndDateAfterStartDate(groups = HearingBookingDetailsGroup.class)
public class HearingBooking implements TranslatableItem {
    @CCD(label = "Type of hearing", showCondition = "type!=\"OTHER\"", searchable = false)
    private HearingType type;
    @CCD(label = "Status", showCondition = "status=\"DO NOT SHOW\"", searchable = false)
    private HearingStatus status;
    @CCD(
            label = "Type of hearing",
            showCondition = "type=\"OTHER\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String typeDetails;
    @CCD(
            label = "Reason",
            showCondition = "type=\"ACCELERATED_DISCHARGE_OF_CARE\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String typeReason;
    @CCD(
            label = "Court",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "HearingVenue"
    )
    private final String venue;
    @CCD(label = "Hearing address", searchable = false)
    private final String customPreviousVenue;
    @CCD(label = "Court address", searchable = false, typeOverride = FieldType.AddressUK)
    private final Address venueCustomAddress;
    @CCD(label = "In person or remote", showCondition = "attendance != \"*\"", searchable = false)
    private final HearingPresence presence;
    @CCD(label = "Hearing attendance", searchable = false)
    private final List<HearingAttendance> attendance;
    @CCD(label = "Hearing attendance details", searchable = false, typeOverride = FieldType.TextArea)
    private final String attendanceDetails;
    @CCD(label = "Pre-hearing attendance", searchable = false)
    private final String preAttendanceDetails;
    @CCD(label = "Start date and time", hint = "Use 24 hour format")
    @TimeNotMidnight(message = "Enter a valid start time", groups = HearingBookingDetailsGroup.class)
    @Future(message = "Enter a start date in the future", groups = HearingBookingDetailsGroup.class)
    private final LocalDateTime startDate;
    @CCD(label = "End date and time", hint = "Use 24 hour format", showCondition = "endDateDerived=\"No\"")
    @TimeNotMidnight(message = "Enter a valid end time", groups = HearingBookingDetailsGroup.class)
    @Future(message = "Enter an end date in the future", groups = HearingBookingDetailsGroup.class)
    private final LocalDateTime endDate;
    @CCD(label = "Vacated date", searchable = false)
    private final LocalDate vacatedDate;
    @CCD(label = "Hearing duration", showCondition = "endDateDerived=\"Yes\"", searchable = false)
    private final String hearingDuration;
    @CCD(label = "Derived end date", showCondition = "startDate=\"DO_NOT_SHOW\"", searchable = false)
    private final String endDateDerived;
    @CCD(label = "days", showCondition = "startDate=\"DO_NOT_SHOW\"", searchable = false)
    private final Integer hearingDays;
    @CCD(label = "minutes", showCondition = "startDate=\"DO_NOT_SHOW\"", searchable = false)
    private final Integer hearingMinutes;
    @CCD(label = "hours", showCondition = "startDate=\"DO_NOT_SHOW\"", searchable = false)
    private final Integer hearingHours;
    @CCD(label = "Hearing needs booked", searchable = false)
    private final List<HearingNeedsBooked> hearingNeedsBooked;
    @CCD(
            label = "Give details",
            showCondition = "hearingNeedsBooked!=\"NONE\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String hearingNeedsDetails;
    @CCD(
            label = "Additional notes",
            hint = "This will be printed on the notice of hearing, if issued",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String additionalNotes;
    @CCD(label = "Allocated judge or magistrate", searchable = false)
    private final String allocatedJudgeLabel;
    @CCD(label = "Hearing judge or magistrate", searchable = false)
    private final String hearingJudgeLabel;
    @CCD(label = "Justices' Legal Adviser's full name", searchable = false)
    private final String legalAdvisorLabel;
    //judgeAndLegalAdvisor field not shown in tab for new hearings but shown for hearings before FPLA-2030
    @CCD(
            label = "Judge and Justices' Legal Adviser",
            showCondition = "hearingJudgeLabel!=\"*\" AND allocatedJudgeLabel!=\"*\"",
            searchable = false
    )
    private JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    @CCD(
            label = " ",
            showCondition = "type = \"DO NOT SHOW\"",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Others"
    )
    private final List<Element<Other>> others;
    @CCD(label = "Others notified")
    private final String othersNotified;
    @CCD(
            label = "Id of the Case Management Order for this hearing",
            showCondition = "judgeAndLegalAdvisor=\"DO NOT SHOW\"",
            searchable = false,
            typeOverride = FieldType.Text
    )
    private UUID caseManagementOrderId;
    @CCD(
            label = "Notice of hearing",
            categoryID = "hearingNotices",
            searchable = false,
            typeOverride = FieldType.Document
    )
    private DocumentReference noticeOfHearing;
    @CCD(
            label = "Translated document",
            categoryID = "hearingNotices",
            searchable = false,
            typeOverride = FieldType.Document
    )
    private final DocumentReference translatedNoticeOfHearing;
    @CCD(label = "Welsh translation upload time", showCondition = "translationRequirements=\"DO_NOT_SHOW\"")
    private final LocalDateTime translationUploadDateTime;
    @CCD(label = " ", showCondition = "translationRequirements=\"DO_NOT_SHOW\"", typeOverride = FieldType.Text)
    private LanguageTranslationRequirement translationRequirements;
    @CCD(label = " ", showCondition = "previousHearingVenue=\"DO NOT SHOW\"", searchable = false)
    private final PreviousHearingVenue previousHearingVenue;
    @CCD(label = " ", showCondition = "status=\"DO NOT SHOW\"", searchable = false)
    private String cancellationReason;
    @CCD(label = " ", showCondition = "status=\"DO NOT SHOW\"", searchable = false)
    private String housekeepReason;
    @CCD(label = " ", showCondition = "type=\"DO NOT SHOW\"", typeOverride = FieldType.Document)
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

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Sent for translation",
          showCondition = "needTranslation=\"YES\" AND translatedNoticeOfHearing!=\"*\"",
          typeOverride = FieldType.Label
  )
  private String sentForTranslationLabel;
  // ==== end synthesised definition-only fields ====
}
