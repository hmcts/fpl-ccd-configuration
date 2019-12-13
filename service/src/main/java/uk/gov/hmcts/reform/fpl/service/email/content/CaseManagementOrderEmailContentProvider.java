package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;

@Service
public class CaseManagementOrderEmailContentProvider extends AbstractEmailContentProvider {
    private final ObjectMapper objectMapper;

    protected CaseManagementOrderEmailContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                                      DateFormatterService dateFormatterService,
                                                      HearingBookingService hearingBookingService,
                                                      ObjectMapper objectMapper) {
        super(uiBaseUrl, dateFormatterService, hearingBookingService);
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> buildCMOIssuedCaseLinkNotificationParameters(final CaseDetails caseDetails,
                                                                            final String recipientName) {
        return ImmutableMap.<String, Object>builder()
            .putAll(buildCommonCMONotificationParameters(caseDetails))
            .put("localAuthorityNameOrRepresentativeFullName", recipientName)
            .build();
    }

    private Map<String, String> buildCommonCMONotificationParameters(final CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        final String subjectLine = buildSubjectLine(caseData);

        return ImmutableMap.of(
            "subjectLineWithHearingDate", buildSubjectLineWithHearingBookingDateSuffix(subjectLine,
                caseData.getHearingDetails()),
            "reference", String.valueOf(caseDetails.getId()),
            "caseUrl", String.format("%1$s/case/%2$s/%3$s/%4$s",
                uiBaseUrl, JURISDICTION, CASE_TYPE, caseDetails.getId())
        );
    }
}
