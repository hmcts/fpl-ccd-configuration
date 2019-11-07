package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.time.format.FormatStyle;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;

@Service
public class C21OrderEmailContentProvider extends AbstractEmailContentProvider {

    private final HearingBookingService hearingBookingService;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final ObjectMapper objectMapper;
    private final DateFormatterService dateFormatterService;

    protected C21OrderEmailContentProvider(@Value("${ccd.ui.base.url}")String uiBaseUrl,
                                           ObjectMapper objectMapper,
                                           HearingBookingService hearingBookingService,
                                           LocalAuthorityNameLookupConfiguration
                                               localAuthorityNameLookupConfiguration,
                                           DateFormatterService dateFormatterService) {
        super(uiBaseUrl);
        this.objectMapper = objectMapper;
        this.hearingBookingService = hearingBookingService;
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.dateFormatterService = dateFormatterService;
    }

    public Map<String, Object> buildC21OrderNotification(final CaseDetails caseDetails,
                                                         final String localAuthorityCode) {
        // Validation within our frontend ensures that the following data is present
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

        final String subjectLine = buildSubjectLine(caseData);

        return ImmutableMap.of(
            "subjectLine", subjectLine,
            "localAuthorityOrCafcass", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode),
            "hearingDetailsCallout", buildSubjectLineWithHearingBookingDateSuffix(subjectLine, caseData),
            "reference", String.valueOf(caseDetails.getId()),
            "caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId()

        );
    }

    private String buildSubjectLineWithHearingBookingDateSuffix(final String subjectLine, final CaseData caseData) {
        String hearingDate = "";
        if (!ObjectUtils.isEmpty(caseData.getHearingDetails())) {
            hearingDate = " hearing " + dateFormatterService.formatLocalDateToString(
                hearingBookingService.getMostUrgentHearingBooking(caseData.getHearingDetails()).getDate(),
                FormatStyle.MEDIUM);
        }

        return Stream.of(subjectLine, hearingDate)
            .filter(StringUtils::isNotBlank)
            .collect(joining(", "));
    }
}
