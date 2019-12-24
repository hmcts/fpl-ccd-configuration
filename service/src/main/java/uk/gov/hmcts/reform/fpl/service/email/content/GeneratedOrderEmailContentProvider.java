package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;

@Service
public class GeneratedOrderEmailContentProvider extends AbstractEmailContentProvider {

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final ObjectMapper objectMapper;

    public GeneratedOrderEmailContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                              ObjectMapper objectMapper,
                                              HearingBookingService hearingBookingService,
                                              LocalAuthorityNameLookupConfiguration
                                                  localAuthorityNameLookupConfiguration,
                                              DateFormatterService dateFormatterService) {
        super(uiBaseUrl, dateFormatterService, hearingBookingService);
        this.objectMapper = objectMapper;
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
    }

    public Map<String, Object> buildOrderNotificationParametersForLocalAuthority(
        final CaseDetails caseDetails, final String localAuthorityCode, final String mostRecentUploadedDocumentUrl) {
        return ImmutableMap.<String, Object>builder()
            .putAll(commonOrderNotificationParameters(caseDetails, mostRecentUploadedDocumentUrl))
            .put("localAuthorityOrCafcass",
                localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .build();
    }

    private Map<String, Object> commonOrderNotificationParameters(final CaseDetails caseDetails,
                                                                  final String linkToDocument) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        final String subjectLine = buildSubjectLine(caseData);
        return ImmutableMap.of(
            "subjectLine", subjectLine,
            "linkToDocument", linkToDocument,
            "hearingDetailsCallout", buildSubjectLineWithHearingBookingDateSuffix(subjectLine,
                caseData.getHearingDetails()),
            "reference", String.valueOf(caseDetails.getId()),
            "caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId()
        );
    }
}
