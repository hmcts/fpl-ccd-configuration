package uk.gov.hmcts.reform.fpl.utils;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

import java.time.format.FormatStyle;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmailNotificationHelper {

    public static String buildSubjectLine(final CaseData caseData) {
        final String respondentlastName = getFirstRespondentLastName(caseData.getRespondents1());
        final String familyManCaseNumber = defaultIfNull(caseData.getFamilyManCaseNumber(), "");

        return Stream.of(respondentlastName, familyManCaseNumber)
            .filter(StringUtils::isNotBlank)
            .collect(joining(", "));
    }

    public static String buildSubjectLineWithHearingBookingDateSuffix(final CaseData caseData,
                                                               HearingBooking hearingBooking) {
        String subjectLine = buildSubjectLine(caseData);
        String hearingDateText = buildHearingDateText(hearingBooking);

        return buildCommonSubjectLine(subjectLine, hearingDateText);
    }

    public static String buildSubjectLineWithoutHearingBookingDateSuffix(final CaseData caseData) {
        String subjectLine = buildSubjectLine(caseData);

        return buildCommonSubjectLine(subjectLine, EMPTY);
    }

    private static String buildCommonSubjectLine(String subjectLine, String hearingDateText) {
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

    private static String buildHearingDateText(HearingBooking hearingBooking) {
        return " hearing " + formatLocalDateToString(hearingBooking
            .getStartDate().toLocalDate(), FormatStyle.MEDIUM);
    }
}
