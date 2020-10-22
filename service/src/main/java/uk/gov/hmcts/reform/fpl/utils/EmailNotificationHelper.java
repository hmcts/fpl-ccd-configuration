package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.format.FormatStyle;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

public class EmailNotificationHelper {

    private EmailNotificationHelper() {
    }

    public static String buildSubjectLine(final String familyManCaseNumber,
                                          final List<Element<Respondent>> respondents) {
        final String respondentlastName = getFirstRespondentLastName(respondents);
        final String familyMan = defaultIfNull(familyManCaseNumber, "");

        return Stream.of(respondentlastName, familyMan)
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

    public static String formatCaseUrl(String uiBaseUrl, Long caseId) {
        return String.format("%s/cases/case-details/%s", uiBaseUrl, caseId);
    }

    public static String formatCaseUrl(String uiBaseUrl, Long caseId, String tab) {
        String caseUrl = formatCaseUrl(uiBaseUrl, caseId);
        return isBlank(tab) ? caseUrl : String.format("%s#%s", caseUrl, tab);
    }

    public static String buildCallout(final CaseData caseData) {
        return "^" + buildSubjectLineWithHearingBookingDateSuffix(caseData.getFamilyManCaseNumber(),
            caseData.getRespondents1(),
            caseData.getFirstHearing().orElse(null));
    }

    private static String buildHearingDateText(HearingBooking hearingBooking) {
        return " hearing " + formatLocalDateToString(hearingBooking
            .getStartDate().toLocalDate(), FormatStyle.MEDIUM);
    }
}
