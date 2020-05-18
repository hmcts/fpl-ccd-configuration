package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.util.Map;

@Service
public class FailedPBAPaymentContentProvider extends AbstractEmailContentProvider {

    public Map<String, Object> buildCtscNotificationParameters(CaseDetails caseDetails,
                                                               ApplicationType applicationType) {
        return ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters(applicationType))
            .put("caseUrl", getCaseUrl(caseDetails.getId()))
            .build();
    }

    public Map<String, Object> buildLANotificationParameters(ApplicationType applicationType) {
        return buildCommonNotificationParameters(applicationType);
    }

    private Map<String, Object> buildCommonNotificationParameters(ApplicationType applicationType) {
        return Map.of("applicationType", applicationType.getType());
    }
}
