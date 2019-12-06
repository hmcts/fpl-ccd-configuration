package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.time.format.FormatStyle;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;

public class EmailNotificationHelper {
    private static final HearingBookingService hearingBookingService = new HearingBookingService();
    private static final DateFormatterService dateFormatterService = new DateFormatterService();

    private EmailNotificationHelper() {
    }

    public static String buildSubjectLine(final CaseData caseData) {
        final String lastName = getFirstRespondentLastName(caseData);
        final String familyManCaseNumber = defaultIfNull(caseData.getFamilyManCaseNumber(), "");

        return Stream.of(lastName, familyManCaseNumber)
            .filter(StringUtils::isNotBlank)
            .collect(joining(", "));
    }

    public static String buildSubjectLineWithHearingBookingDateSuffix(final String subjectLine,
                                                                      final List<Element<HearingBooking>>
                                                                          hearingDetails) {
        String hearingDate = "";
        if (isNotEmpty(hearingDetails)) {
            hearingDate = " hearing " + dateFormatterService.formatLocalDateToString(
                hearingBookingService.getMostUrgentHearingBooking(
                    hearingDetails).getStartDate().toLocalDate(), FormatStyle.MEDIUM);
        }
        return Stream.of(subjectLine, hearingDate)
            .filter(StringUtils::isNotBlank)
            .collect(joining(","));
    }

    private static String getFirstRespondentLastName(final CaseData caseData) {
        return isEmpty(caseData.getRespondents1()) ? "" : caseData.getRespondents1()
            .stream()
            .filter(Objects::nonNull)
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .findFirst()
            .map(Respondent::getParty)
            .map(RespondentParty::getLastName)
            .orElse("");
    }
}
