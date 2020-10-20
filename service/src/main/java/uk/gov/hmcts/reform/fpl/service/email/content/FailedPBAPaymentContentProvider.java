package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.payment.FailedPBANotificationData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C2_APPLICATION;

@Service
public class FailedPBAPaymentContentProvider extends AbstractEmailContentProvider {

    public FailedPBANotificationData buildCtscNotificationParameters(CaseData caseData,
                                                                     ApplicationType applicationType) {
        String tab = applicationType.equals(C2_APPLICATION) ? "C2Tab" : "";
        return FailedPBANotificationData.builder()
            .caseUrl(getCaseUrl(caseData.getId(), tab))
            .applicationType(applicationType.getType())
            .build();
    }

    public FailedPBANotificationData buildLANotificationParameters(ApplicationType applicationType) {
        return FailedPBANotificationData.builder()
            .applicationType(applicationType.getType())
            .build();
    }
}
