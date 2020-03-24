package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Service
public class C2UploadedEmailContentProvider extends AbstractEmailContentProvider {

    private final ObjectMapper objectMapper;

    @Autowired
    protected C2UploadedEmailContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                             ObjectMapper objectMapper,
                                             HearingBookingService hearingBookingService) {
        super(uiBaseUrl, hearingBookingService);
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> buildC2UploadNotification(final CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        final String subjectLine = EmailNotificationHelper.buildSubjectLine(caseData);
        return Map.of(
            "subjectLine", subjectLine,
            "hearingDetailsCallout", subjectLine,
            "reference", String.valueOf(caseDetails.getId()),
            "caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId()
        );
    }
}
