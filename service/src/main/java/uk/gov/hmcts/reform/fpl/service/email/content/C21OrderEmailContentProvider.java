package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.time.format.FormatStyle;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;

@Service
public class C21OrderEmailContentProvider extends AbstractEmailContentProvider {

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final ObjectMapper objectMapper;

    public C21OrderEmailContentProvider(@Value("${ccd.ui.base.url}")String uiBaseUrl,
                                           ObjectMapper objectMapper,
                                           HearingBookingService hearingBookingService,
                                           LocalAuthorityNameLookupConfiguration
                                               localAuthorityNameLookupConfiguration,
                                           DateFormatterService dateFormatterService,
                                           CafcassLookupConfiguration cafcassLookupConfiguration) {
        super(uiBaseUrl, dateFormatterService, hearingBookingService);
        this.objectMapper = objectMapper;
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.cafcassLookupConfiguration = cafcassLookupConfiguration;
    }

    public Map<String, Object> buildC21OrderNotificationParametersForCafcass(final CaseDetails caseDetails,
                                                                             final String localAuthorityCode,
                                                                          final String mostRecentUploadedDocumentUrl) {
        return ImmutableMap.<String, Object>builder()
            .putAll(commonC21NotificationParameters(caseDetails, mostRecentUploadedDocumentUrl))
            .put("localAuthorityOrCafcass", cafcassLookupConfiguration.getCafcass(localAuthorityCode).getName())
            .build();
    }

    public Map<String, Object> buildC21OrderNotificationParametersForLocalAuthority(final CaseDetails caseDetails,
                                                                                    final String localAuthorityCode,
                                                                          final String mostRecentUploadedDocumentUrl) {
        return ImmutableMap.<String, Object>builder()
            .putAll(commonC21NotificationParameters(caseDetails, mostRecentUploadedDocumentUrl))
            .put("localAuthorityOrCafcass",
                localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .build();
    }

    private String buildSubjectLineWithHearingBookingDateSuffix(final String subjectLine, final CaseData caseData) {
        String hearingDate = "";
        if (isNotEmpty(caseData.getHearingDetails())) {
            hearingDate = " hearing " + dateFormatterService.formatLocalDateToString(
                hearingBookingService.getMostUrgentHearingBooking(caseData.getHearingDetails()).getDate(),
                FormatStyle.MEDIUM);
        }
        return Stream.of(subjectLine, hearingDate)
            .filter(StringUtils::isNotBlank)
            .collect(joining(","));
    }

    private Map<String, Object> commonC21NotificationParameters(final CaseDetails caseDetails,
                                                                final String linkToDocument) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        final String subjectLine = buildSubjectLine(caseData);
        return ImmutableMap.of(
            "subjectLine", subjectLine,
            "linkToDocument", linkToDocument,
            "hearingDetailsCallout", buildSubjectLineWithHearingBookingDateSuffix(subjectLine, caseData),
            "reference", String.valueOf(caseDetails.getId()),
            "caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId()
        );
    }
}
