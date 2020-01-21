package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.time.format.FormatStyle;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

public class EmailNotificationHelper {
    private static final HearingBookingService hearingBookingService = new HearingBookingService();
    private static final DateFormatterService dateFormatterService = new DateFormatterService();

    private EmailNotificationHelper() {
    }

    public static String buildSubjectLine(final CaseData caseData) {
        final String respondentlastName = getFirstRespondentLastName(caseData.getRespondents1());
        final String familyManCaseNumber = defaultIfNull(caseData.getFamilyManCaseNumber(), "");

        return Stream.of(respondentlastName, familyManCaseNumber)
            .filter(StringUtils::isNotBlank)
            .collect(joining(", "));
    }

    public static String buildSubjectLineWithHearingBookingDateSuffix(final String subjectLine,
                                                                      final List<Element<HearingBooking>>
                                                                          hearingBookings) {
        String hearingDateText = "";
        if (isNotEmpty(hearingBookings)) {
            hearingDateText = buildHearingDateText(hearingBookings);
        }

        return Stream.of(subjectLine, hearingDateText)
            .filter(StringUtils::isNotBlank)
            .collect(joining(","));
    }

    private static String buildHearingDateText(final List<Element<HearingBooking>> hearingBookings) {
        return " hearing " + dateFormatterService.formatLocalDateToString(
            hearingBookingService.getMostUrgentHearingBooking(
                hearingBookings).getStartDate().toLocalDate(), FormatStyle.MEDIUM);
    }
}
