package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance;
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
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.trim;
import static uk.gov.hmcts.reform.fpl.model.configuration.Display.Due.BY;
import static uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService.HEARING_VENUE_ID_OTHER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getLegalAdvisorName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseDataExtractionService {
    private static final String REMOTE_HEARING_VENUE = "Remote hearing at %s. Details and instructions will be "
        + "sent by the local court.";

    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final HearingVenueLookUpService hearingVenueLookUpService;

    public String getCourtName(String localAuthority) {
        return hmctsCourtLookupConfiguration.getCourt(localAuthority).getName();
    }

    public String getHearingTime(HearingBooking hearingBooking) {
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

    public String getApplicantName(List<Element<Applicant>> applicants) {
        Applicant applicant = applicants.get(0).getValue();
        return ofNullable(applicant.getParty())
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
            .hearingAttendance(getHearingAttendance(hearing))
            .hearingAttendanceDetails(hearing.getAttendanceDetails())
            .preHearingAttendance(hearing.getPreAttendanceDetails())
            .hearingTime(getHearingTime(hearing))
            .hearingJudgeTitleAndName(judgeAndLegalAdvisor.getJudgeTitleAndName())
            .hearingLegalAdvisorName(judgeAndLegalAdvisor.getLegalAdvisorName())
            .build();
    }

    private String buildHearingVenue(HearingBooking hearing) {
        HearingVenue venue = hearingVenueLookUpService.getHearingVenue(hearing);
        if (venue.getAddress() != null) {
            if (hearing.isRemote()) {
                String venueName = HEARING_VENUE_ID_OTHER.equals(venue.getHearingVenueId())
                    ? venue.getAddress().getAddressLine1() : venue.getVenue();
                // assuming that the building name is in address line 1
                return String.format(REMOTE_HEARING_VENUE, venueName);
            } else {
                return hearingVenueLookUpService.buildHearingVenue(venue);
            }
        } else {
            // enters this if:
            //  • the first hearing uses a custom venue address
            //  • the second hearing uses the same venue
            String previousAddress = hearing.getCustomPreviousVenue();
            if (hearing.isRemote()) {
                // going to have to assume that the building name of the venue is before the first comma,
                // but the user could have entered anything, by limiting to 0 even if the string is empty something
                // is still returned
                String[] splitAddress = previousAddress.split(",", 0);
                return String.format(REMOTE_HEARING_VENUE, splitAddress[0]);
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
            .gender(child.getGender())
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
}
