package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.payment.FailedPBANotificationData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.OTHER_APPLICATIONS;

@Service
public class FailedPBAPaymentContentProvider extends AbstractEmailContentProvider {

    public FailedPBANotificationData getCtscNotifyData(CaseData caseData,
                                                       List<ApplicationType> applicationTypes,
                                                       String applicantName) {
        String applicationNames = applicationTypes.stream()
            .map(ApplicationType::getType).collect(Collectors.joining(","));

        return FailedPBANotificationData.builder()
            .caseUrl(applicationTypes.contains(C110A_APPLICATION) ? getCaseUrl(caseData.getId())
                : getCaseUrl(caseData.getId(), OTHER_APPLICATIONS))
            .applicationType(applicationNames)
            .applicant(applicantName)
            .build();
    }

    public FailedPBANotificationData getLocalAuthorityNotifyData(List<ApplicationType> applicationTypes,
                                                                 Long caseReference) {
        String applicationNames = applicationTypes.stream()
            .map(ApplicationType::getType).collect(Collectors.joining(","));
        return FailedPBANotificationData.builder()
            .applicationType(applicationNames)
            .caseUrl(applicationTypes.contains(C110A_APPLICATION) ? getCaseUrl(caseReference)
                : getCaseUrl(caseReference, OTHER_APPLICATIONS))
            .build();
    }
}
