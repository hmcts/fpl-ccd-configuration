package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Component
public class EmailNotificationHelper {

    private static final String GOV_NOTIFY_INSET_TEXT_STYLING = "^";

    public String getEldestChildLastName(List<Element<Child>> children) {
        return unwrapElements(children).stream()
            .map(Child::getParty)
            .filter(child -> null != child.getDateOfBirth())
            .min(comparing(ChildParty::getDateOfBirth, nullsLast(naturalOrder())))
            .map(ChildParty::getLastName)
            .orElse("");
    }

    public String getEldestChildLastName(CaseData caseData) {
        return getEldestChildLastName(caseData.getAllChildren());
    }

    public static String buildSubjectLine(final String familyManCaseNumber,
                                          final List<Element<Respondent>> respondents) {
        final String respondentLastName = getFirstRespondentLastName(respondents);
        final String familyMan = defaultIfNull(familyManCaseNumber, "");

        return Stream.of(respondentLastName, familyMan)
            .filter(StringUtils::isNotBlank)
            .collect(joining(", "));
    }

    public static String buildSubjectLineWithHearingBookingDateSuffix(final String familyManCaseNumber,
                                                                      final List<Element<Respondent>> respondents,
                                                                      final HearingBooking hearingBooking) {
        String subjectLine = buildSubjectLine(familyManCaseNumber, respondents);
        String hearingDateText = "";

        if (hearingBooking != null) {
            hearingDateText = buildHearingDateText(hearingBooking);
        }

        return Stream.of(subjectLine, hearingDateText)
            .filter(StringUtils::isNotBlank)
            .collect(joining(","));
    }

    public static String buildCallout(final CaseData caseData) {
        return buildSubjectLineWithHearingBookingDateSuffix(caseData.getFamilyManCaseNumber(),
            caseData.getRespondents1(),
            caseData.getFirstHearing().orElse(null));
    }

    public static String buildCalloutWithNextHearing(final CaseData caseData, LocalDateTime time) {
        return GOV_NOTIFY_INSET_TEXT_STYLING + buildUnformattedCalloutWithNextHearing(caseData, time);
    }

    public static String buildCalloutWithChildNameForNextHearing(final CaseData caseData, Child child) {
        StringBuilder childCallout = new StringBuilder()
            .append(GOV_NOTIFY_INSET_TEXT_STYLING)
            .append(child.getParty().getFullName());

        Optional.ofNullable(caseData.getFamilyManCaseNumber())
            .ifPresent(familyManCaseNumber -> childCallout.append(", ").append(familyManCaseNumber));

        return childCallout.toString();
    }

    public static String buildUnformattedCalloutWithNextHearing(final CaseData caseData, LocalDateTime time) {
        return buildSubjectLineWithHearingBookingDateSuffix(caseData.getFamilyManCaseNumber(),
            caseData.getRespondents1(),
            caseData.getNextHearingAfter(time).orElse(null));
    }

    public static List<String> getDistinctGatekeeperEmails(List<Element<EmailAddress>> emailCollection) {
        return unwrapElements(emailCollection)
            .stream()
            .map(EmailAddress::getEmail)
            .distinct()
            .collect(Collectors.toList());
    }

    private static String buildHearingDateText(HearingBooking hearingBooking) {
        return " hearing " + formatLocalDateToString(hearingBooking
            .getStartDate().toLocalDate(), FormatStyle.MEDIUM);
    }
}
