package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;

@Service
public class GeneratedOrderEmailContentProvider extends AbstractEmailContentProvider {
    private final LocalAuthorityNameLookupConfiguration config;

    @Autowired
    protected GeneratedOrderEmailContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                              LocalAuthorityNameLookupConfiguration config) {
        super(uiBaseUrl);
        this.config = config;
    }

    public Map<String, Object> buildOrderNotificationParametersForLocalAuthority(
        final CaseDetails caseDetails, final String localAuthorityCode, final String mostRecentUploadedDocumentUrl) {
        return ImmutableMap.<String, Object>builder()
            .putAll(commonOrderNotificationParameters(caseDetails, mostRecentUploadedDocumentUrl))
            .put("localAuthorityOrCafcass", config.getLocalAuthorityName(localAuthorityCode))
            .build();
    }

    private Map<String, Object> commonOrderNotificationParameters(final CaseDetails caseDetails,
                                                                  final String linkToDocument) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
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
