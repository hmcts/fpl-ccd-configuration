package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudge;
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
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trim;
import static uk.gov.hmcts.reform.fpl.enums.HearingDuration.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.configuration.Display.Due.BY;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getLegalAdvisorName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseDataExtractionService {
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final CourtService courtService;

    private final static String HEARING_TIME_FORMAT = "%s - %s";
    protected static final String REMOTE_HEARING_VENUE = "Remote hearing at %s. Details and instructions will be "
        + "sent by the local court.";

    public String getCourtName(CaseData caseData) {
        return courtService.getCourtName(caseData);
    }

    public String getHearingTime(HearingBooking hearingBooking) {
        String hearingTime;
        final LocalDateTime startDate = hearingBooking.getStartDate();
        final LocalDateTime endDate = hearingBooking.getEndDate();

        // if the duration type is "days", display start/end date as date instead of date-time
        boolean isDayDurationType = isNotEmpty(hearingBooking.getHearingDuration())
                                    && hearingBooking.getHearingDuration().toLowerCase()
                                        .contains(DAYS.getType().toLowerCase());

        if (isDayDurationType) {
            hearingTime = String.format(HEARING_TIME_FORMAT, formatDate(startDate), formatDate(endDate));
        } else if (hearingBooking.hasDatesOnSameDay()) {
            // Example 3:30pm - 5:30pm
            hearingTime = String.format(HEARING_TIME_FORMAT, formatTime(startDate), formatTime(endDate));
        } else {
            // Example 18 June, 3:40pm - 19 June, 2:30pm
            hearingTime = String.format(HEARING_TIME_FORMAT, formatDateTime(startDate), formatDateTime(endDate));
        }

        return hearingTime;
    }

    public Optional<String> getHearingDateIfHearingsOnSameDay(HearingBooking hearingBooking) {
        String hearingDate = null;

        // If they are on same day, then return formatted date
        // and If they aren't on the same day, then return nothing
        if (hearingBooking.hasDatesOnSameDay()) {
            hearingDate = formatLocalDateToString(hearingBooking.getStartDate().toLocalDate(), FormatStyle.LONG);
        }

        return Optional.ofNullable(hearingDate);
    }

    public List<DocmosisChild> getChildrenDetails(List<Element<Child>> children) {
        return children.stream()
            .map(element -> element.getValue().getParty())
            .map(this::buildChild)
            .distinct()
            .collect(toList());
    }

    public String getApplicantName(CaseData caseData) {
        if (ObjectUtils.isNotEmpty(caseData.getLocalAuthorities())) {
            return ofNullable(caseData.getDesignatedLocalAuthority())
                .map(LocalAuthority::getName)
                .orElse(ofNullable(caseData.getLocalAuthorities().get(0))
                    .map(Element::getValue)
                    .map(LocalAuthority::getName).orElse(""));
        }

        return ofNullable(caseData.getAllApplicants().get(0).getValue().getParty())
            .map(ApplicantParty::getOrganisationName)
            .orElse("");
    }

    public List<DocmosisRespondent> getRespondentsNameAndRelationship(List<Element<Respondent>> respondents) {
        return respondents.stream()
            .map(element -> element.getValue().getParty())
            .map(this::buildRespondent)
            .collect(toList());
    }


    public DocmosisJudgeAndLegalAdvisor getJudgeAndLegalAdvisor(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        return DocmosisJudgeAndLegalAdvisor.builder()
            .judgeTitleAndName(formatJudgeTitleAndName(judgeAndLegalAdvisor))
            .legalAdvisorName(getLegalAdvisorName(judgeAndLegalAdvisor))
            .build();
    }

    public DocmosisJudge getAllocatedJudge(JudgeAndLegalAdvisor allocatedJudge) {
        return DocmosisJudge.builder()
            .judgeTitleAndName(formatJudgeTitleAndName(allocatedJudge))
            .build();
    }

    public DocmosisHearingBooking getHearingBookingData(HearingBooking hearingBooking) {
        return ofNullable(hearingBooking)
            .map(this::buildHearingBooking)
            .orElse(DocmosisHearingBooking.builder().build());
    }

    public DocmosisDirection.Builder baseDirection(Direction direction, int index) {
        return baseDirection(direction, index, emptyList());
    }

    public DocmosisDirection.Builder baseDirection(Direction direction, int index,
                                                   List<DirectionConfiguration> config) {
        return DocmosisDirection.builder()
            .assignee(direction.getAssignee())
            .title(formatTitle(index, direction, config))
            .body(trim(direction.getDirectionText()));
    }

    private String formatTitle(int index, Direction direction, List<DirectionConfiguration> directionConfigurations) {

        // default values here cover edge case where direction title is not found in configuration.
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

        // create direction display title for docmosis in format "index. directionTitle [(by / on) date]"
        return format("%d. %s %s", index, direction.getDirectionType(),
            formatTitleDate(direction.getDateToBeCompletedBy(), config.pattern, config.due)).trim();
    }

    private String formatTitleDate(LocalDateTime date, String pattern, Display.Due due) {
        if (date == null) {
            return "";
        }

        return due.toString().toLowerCase() + " " + formatLocalDateTimeBaseUsingFormat(date, pattern);
    }

    private DocmosisHearingBooking buildHearingBooking(HearingBooking hearing) {
        String hearingVenue = buildHearingVenue(hearing);

        DocmosisJudgeAndLegalAdvisor judgeAndLegalAdvisor = getJudgeAndLegalAdvisor(hearing.getJudgeAndLegalAdvisor());

        return DocmosisHearingBooking.builder()
            .hearingDate(getHearingDateIfHearingsOnSameDay(hearing).orElse(""))
            .hearingVenue(hearingVenue)
            .hearingAttendance(getHearingAttendance(hearing).orElse(null))
            .hearingAttendanceDetails(hearing.getAttendanceDetails())
            .preHearingAttendance(hearing.getPreAttendanceDetails())
            .hearingTime(getHearingTime(hearing))
            .hearingDuration(hearing.getHearingDuration())
            .hearingJudgeTitleAndName(judgeAndLegalAdvisor.getJudgeTitleAndName())
            .hearingLegalAdvisorName(judgeAndLegalAdvisor.getLegalAdvisorName())
            .hearingStartDate(formatLocalDateTimeBaseUsingFormat(hearing.getStartDate(), DATE_TIME))
            .hearingEndDate(formatLocalDateTimeBaseUsingFormat(hearing.getEndDate(), DATE_TIME))
            .endDateDerived(hearing.getEndDateDerived())
            .build();
    }

    private String buildHearingVenue(HearingBooking hearing) {
        if (hearing.getPreviousHearingVenue() != null
            && YES.getValue().equals(hearing.getPreviousHearingVenue().getUsePreviousVenue())) {
            String venueAddress = hearing.getPreviousHearingVenue().getPreviousVenue();
            return hearing.isRemote() ? format(REMOTE_HEARING_VENUE, venueAddress) : venueAddress;
        }
        HearingVenue venue = hearingVenueLookUpService.getHearingVenue(hearing);
        if (venue.getAddress() != null) {
            String venueAddress = hearingVenueLookUpService.buildHearingVenue(venue);
            if (hearing.isRemote()) {
                return String.format(REMOTE_HEARING_VENUE, venueAddress);
            } else {
                return venueAddress;
            }
        } else {
            // enters this if:
            //  • the first hearing uses a custom venue address
            //  • the second hearing uses the same venue
            String previousAddress = hearing.getCustomPreviousVenue();
            if (hearing.isRemote()) {
                return String.format(REMOTE_HEARING_VENUE, previousAddress);
            } else {
                return previousAddress;
            }
        }
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
            .gender(Optional.ofNullable(child.getGender()).map(ChildGender::getLabel).orElse(null))
            .dateOfBirth(ofNullable(child.getDateOfBirth())
                .map(dob -> formatLocalDateToString(child.getDateOfBirth(), LONG))
                .orElse(null))
            .build();
    }

    public Optional<String> getHearingAttendance(HearingBooking hearingBooking) {
        return Optional.ofNullable(hearingBooking.getAttendance())
            .filter(ObjectUtils::isNotEmpty)
            .map(attendances -> attendances.stream()
                .map(HearingAttendance::getLabel)
                .map(StringUtils::uncapitalize)
                .collect(joining(", ")))
            .map(StringUtils::capitalize);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return formatLocalDateTimeBaseUsingFormat(dateTime, "d MMMM, h:mma");
    }

    private String formatTime(LocalDateTime dateTime) {
        return formatLocalDateTimeBaseUsingFormat(dateTime, "h:mma");
    }

    private String formatDate(LocalDateTime dateTime) {
        return formatLocalDateTimeBaseUsingFormat(dateTime, "d MMMM");
    }
}
