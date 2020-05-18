package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C2UploadedEmailContentProvider extends AbstractEmailContentProvider {

    private final EmailNotificationHelper emailNotificationHelper;
    private final ObjectMapper mapper;

    public Map<String, Object> buildC2UploadNotification(final CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        final String subjectLine = emailNotificationHelper.buildSubjectLine(caseData);

        return ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters(caseDetails))
            .put("subjectLine", subjectLine)
            .put("hearingDetailsCallout", subjectLine)
            .put("reference", String.valueOf(caseDetails.getId()))
            .build();
    }

    public Map<String, Object> buildC2UploadPbaPaymentNotTakenNotification(final CaseDetails caseDetails) {
        return buildCommonNotificationParameters(caseDetails);
    }

    private Map<String, Object> buildCommonNotificationParameters(final CaseDetails caseDetails) {
        return Map.of("caseUrl", getCaseUrl(caseDetails.getId()));
    }
}
