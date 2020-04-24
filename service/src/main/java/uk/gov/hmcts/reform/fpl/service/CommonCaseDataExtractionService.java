package uk.gov.hmcts.reform.fpl.service;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRespondent;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.time.format.FormatStyle.LONG;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.trim;
import static uk.gov.hmcts.reform.fpl.model.configuration.Display.Due.BY;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getLegalAdvisorName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommonCaseDataExtractionService {
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final HearingVenueLookUpService hearingVenueLookUpService;

    String getCourtName(String localAuthority) {
        return hmctsCourtLookupConfiguration.getCourt(localAuthority).getName();
    }

    String getHearingTime(HearingBooking hearingBooking) {
        String hearingTime;
        final LocalDateTime startDate = hearingBooking.getStartDate();
        final LocalDateTime endDate = hearingBooking.getEndDate();

        if (hearingBooking.hasDatesOnSameDay()) {
            // Example 3:30pm - 5:30pm
            hearingTime = String.format("%s - %s", formatTime(startDate), formatTime(endDate));
        } else {
            // Example 18 June, 3:40pm - 19 June, 2:30pm
            hearingTime = String.format("%s - %s", formatDateTime(startDate), formatDateTime(endDate));
        }

        return hearingTime;
    }

    Optional<String> getHearingDateIfHearingsOnSameDay(HearingBooking hearingBooking) {
        String hearingDate = null;

        // If they aren't on the same date return nothing
        if (hearingBooking.hasDatesOnSameDay()) {
            hearingDate = formatLocalDateToString(hearingBooking.getStartDate().toLocalDate(), FormatStyle.LONG);
        }

        return Optional.ofNullable(hearingDate);
    }

    List<DocmosisChild> getChildrenDetails(List<Element<Child>> children) {
        return children.stream()
            .map(element -> element.getValue().getParty())
            .map(this::buildChild)
            .collect(toList());
    }

    String getApplicantName(List<Element<Applicant>> applicants) {
        Applicant applicant = applicants.get(0).getValue();
        return ofNullable(applicant.getParty())
            .map(ApplicantParty::getOrganisationName)
            .orElse("");
    }

    List<DocmosisRespondent> getRespondentsNameAndRelationship(List<Element<Respondent>> respondents) {
        return respondents.stream()
            .map(element -> element.getValue().getParty())
            .map(this::buildRespondent)
            .collect(toList());
    }

    String extractPrehearingAttendance(HearingBooking booking) {
        LocalDateTime time = calculatePrehearingAttendance(booking.getStartDate());

        return booking.hasDatesOnSameDay() ? formatTime(time) : formatDateTimeWithYear(time);
    }

    DocmosisJudgeAndLegalAdvisor getJudgeAndLegalAdvisor(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        return DocmosisJudgeAndLegalAdvisor.builder()
            .judgeTitleAndName(formatJudgeTitleAndName(judgeAndLegalAdvisor))
            .legalAdvisorName(getLegalAdvisorName(judgeAndLegalAdvisor))
            .build();
    }

    DocmosisHearingBooking getHearingBookingData(HearingBooking hearingBooking, String value) {
        return ofNullable(hearingBooking).map(this::buildHearingBooking).orElse(getHearingBookingWithDefault(value));
    }

    DocmosisDirection.Builder baseDirection(Direction direction, int index) {
        return baseDirection(direction, index, emptyList());
    }

    DocmosisDirection.Builder baseDirection(Direction direction, int index, List<DirectionConfiguration> config) {
        return DocmosisDirection.builder()
            .assignee(direction.getAssignee())
            .title(formatTitle(index, direction, config))
            .body(trim(direction.getDirectionText()));
    }

    private String formatTitle(int index, Direction direction, List<DirectionConfiguration> directionConfigurations) {

        // default values here cover edge case where direction title is not found in configuration.
        @NoArgsConstructor
        class DateFormattingConfig {
            private String pattern = TIME_DATE;
            private Display.Due due = BY;
        }

        final DateFormattingConfig config = new DateFormattingConfig();

        // find the date configuration values for the given direction
        for (DirectionConfiguration directionConfiguration : directionConfigurations) {
            if (directionConfiguration.getTitle().equals(direction.getDirectionType())) {
                Display display = directionConfiguration.getDisplay();
                config.pattern = display.getTemplateDateFormat();
                config.due = display.getDue();
                break;
            }
        }

        // create direction display title for docmosis in format "index. directionTitle (by / on) date"
        return format("%d. %s %s %s", index, direction.getDirectionType(), lowerCase(config.due.toString()),
            ofNullable(direction.getDateToBeCompletedBy())
                .map(date -> formatLocalDateTimeBaseUsingFormat(date, config.pattern))
                .orElse("unknown"));
    }

    private DocmosisHearingBooking buildHearingBooking(HearingBooking hearing) {
        HearingVenue hearingVenue = hearingVenueLookUpService.getHearingVenue(hearing);
        DocmosisJudgeAndLegalAdvisor judgeAndLegalAdvisor =
            getJudgeAndLegalAdvisor(hearing.getJudgeAndLegalAdvisor());

        return DocmosisHearingBooking.builder()
            .hearingDate(getHearingDateIfHearingsOnSameDay(hearing).orElse(""))
            .hearingVenue(hearingVenueLookUpService.buildHearingVenue(hearingVenue))
            .preHearingAttendance(extractPrehearingAttendance(hearing))
            .hearingTime(getHearingTime(hearing))
            .hearingJudgeTitleAndName(judgeAndLegalAdvisor.getJudgeTitleAndName())
            .hearingLegalAdvisorName(judgeAndLegalAdvisor.getLegalAdvisorName())
            .build();
    }

    private DocmosisHearingBooking getHearingBookingWithDefault(String value) {
        return DocmosisHearingBooking.builder()
            .hearingDate(value)
            .hearingVenue(value)
            .preHearingAttendance(value)
            .hearingTime(value)
            .build();
    }

    private DocmosisRespondent buildRespondent(RespondentParty respondent) {
        return DocmosisRespondent.builder()
            .name(respondent.getFullName())
            .relationshipToChild(respondent.getRelationshipToChild())
            .build();
    }

    private DocmosisChild buildChild(ChildParty child) {
        return DocmosisChild.builder()
            .name(child.getFullName())
            .gender(child.getGender())
            .dateOfBirth(formatLocalDateToString(child.getDateOfBirth(), LONG))
            .build();
    }

    private LocalDateTime calculatePrehearingAttendance(LocalDateTime dateTime) {
        return dateTime.minusHours(1);
    }

    private String formatDateTimeWithYear(LocalDateTime dateTime) {
        return formatLocalDateTimeBaseUsingFormat(dateTime, DATE_TIME);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return formatLocalDateTimeBaseUsingFormat(dateTime, "d MMMM, h:mma");
    }

    private String formatTime(LocalDateTime dateTime) {
        return formatLocalDateTimeBaseUsingFormat(dateTime, "h:mma");
    }
}
