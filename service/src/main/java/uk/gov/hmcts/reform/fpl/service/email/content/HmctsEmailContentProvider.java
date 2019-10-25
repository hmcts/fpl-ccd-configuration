package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Service
public class HmctsEmailContentProvider extends AbstractEmailContentProvider {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    @Autowired
    public HmctsEmailContentProvider(LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                                     HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                                     @Value("${ccd.ui.base.url}") String uiBaseUrl) {
        super(uiBaseUrl);
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
    }

    public Map<String, Object> buildHmctsSubmissionNotification(CaseDetails caseDetails, String localAuthorityCode) {
        return super.getCasePersonalisationBuilder(caseDetails)
            .put("court", hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getName())
            .put("localAuthority", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .build();
    }

    public Map<String, Object> buildC2UploadNotification(final CaseDetails caseDetails) {
        final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        final String subjectLine = EmailNotificationHelper.buildSubjectLine(caseData);
        return Map.of(
            "subjectLine", subjectLine,
            "hearingDetailsCallout", subjectLine,
            "reference", String.valueOf(caseDetails.getId()),
            "caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId()
        );
    }
}
