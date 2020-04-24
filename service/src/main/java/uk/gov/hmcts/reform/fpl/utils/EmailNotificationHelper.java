package uk.gov.hmcts.reform.fpl.utils;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.time.format.FormatStyle;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmailNotificationHelper {

    private final HearingBookingService hearingBookingService;

    public String buildSubjectLine(final CaseData caseData) {
        final String respondentlastName = getFirstRespondentLastName(caseData.getRespondents1());
        final String familyManCaseNumber = defaultIfNull(caseData.getFamilyManCaseNumber(), "");

        return Stream.of(respondentlastName, familyManCaseNumber)
            .filter(StringUtils::isNotBlank)
            .collect(joining(", "));
    }

    public String buildSubjectLineWithHearingBookingDateSuffix(final CaseData caseData,
                                                                      final List<Element<HearingBooking>>
                                                                          hearingBookings) {
        String subjectLine = buildSubjectLine(caseData);
        String hearingDateText = "";
        if (isNotEmpty(hearingBookings)) {
            hearingDateText = buildHearingDateText(hearingBookings);
        }

        return Stream.of(subjectLine, hearingDateText)
            .filter(StringUtils::isNotBlank)
            .collect(joining(","));
    }

    public static String formatCaseUrl(String uiBaseUrl, Long caseId) {
        return String.format("%s/case/%s/%s/%s", uiBaseUrl, JURISDICTION, CASE_TYPE, caseId);
    }

    public static String formatCaseUrl(String uiBaseUrl, Long caseId, String tab) {
        String caseUrl = formatCaseUrl(uiBaseUrl, caseId);
        return isBlank(tab) ? caseUrl : String.format("%s#%s", caseUrl, tab);
    }

    private String buildHearingDateText(final List<Element<HearingBooking>> hearingBookings) {
        return " hearing " + formatLocalDateToString(hearingBookingService.getMostUrgentHearingBooking(hearingBookings)
            .getStartDate().toLocalDate(), FormatStyle.MEDIUM);
    }
}
