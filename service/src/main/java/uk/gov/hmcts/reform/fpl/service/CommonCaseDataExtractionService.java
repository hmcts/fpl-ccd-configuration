package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRespondent;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Optional;

import static java.time.format.FormatStyle.LONG;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getLegalAdvisorName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommonCaseDataExtractionService {
    private static final String DEFAULT = "BLANK - please complete";

    private final HearingVenueLookUpService hearingVenueLookUpService;

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

    DocmosisHearingBooking getHearingBookingData(HearingBooking hearingBooking, String error) {
        return ofNullable(hearingBooking).map(hearing -> {
                HearingVenue hearingVenue = hearingVenueLookUpService.getHearingVenue(hearing);

                return DocmosisHearingBooking.builder()
                    .hearingDate(getHearingDateIfHearingsOnSameDay(hearing).orElse(""))
                    .hearingVenue(hearingVenueLookUpService.buildHearingVenue(hearingVenue))
                    .preHearingAttendance(extractPrehearingAttendance(hearing))
                    .hearingTime(getHearingTime(hearing))
                    .build();
            }
        ).orElse(DocmosisHearingBooking.builder()
            .hearingDate(error)
            .hearingVenue(error)
            .preHearingAttendance(error)
            .hearingTime(error)
            .build());
    }

    private DocmosisRespondent buildRespondent(RespondentParty respondent) {
        return DocmosisRespondent.builder()
            .name(respondent.getFullName())
            .relationshipToChild(defaultIfNull(respondent.getRelationshipToChild(), DEFAULT))
            .build();
    }

    private DocmosisChild buildChild(ChildParty child) {
        return DocmosisChild.builder()
            .name(child.getFullName())
            .gender(defaultIfNull(child.getGender(), DEFAULT))
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
