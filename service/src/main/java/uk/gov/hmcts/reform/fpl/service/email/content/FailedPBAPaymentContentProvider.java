package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.payment.FailedPBANotificationData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C2_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.C2;

@Service
public class FailedPBAPaymentContentProvider extends AbstractEmailContentProvider {

    public FailedPBANotificationData getCtscNotifyData(CaseData caseData,
                                                       ApplicationType applicationType) {
        return FailedPBANotificationData.builder()
            .caseUrl(applicationType.equals(C2_APPLICATION) ? getCaseUrl(caseData.getId(), C2)
                : getCaseUrl(caseData.getId()))
            .applicationType(applicationType.getType())
            .build();
    }

    public FailedPBANotificationData getLocalAuthorityNotifyData(ApplicationType applicationType) {
        return FailedPBANotificationData.builder()
            .applicationType(applicationType.getType())
            .build();
    }
}
