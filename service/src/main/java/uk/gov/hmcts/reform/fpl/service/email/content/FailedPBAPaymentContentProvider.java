package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.payment.FailedPBANotificationData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.A50_PLACEMENT;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.OTHER_APPLICATIONS;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.PLACEMENT;

@Service
public class FailedPBAPaymentContentProvider extends AbstractEmailContentProvider {

    private static final String DELIMITER = ", ";

    public FailedPBANotificationData getCtscNotifyData(CaseData caseData,
                                                       List<ApplicationType> applicationTypes,
                                                       String applicantName) {
        String applicationNames = applicationTypes.stream()
            .map(ApplicationType::getType).collect(Collectors.joining(DELIMITER));

        return FailedPBANotificationData.builder()
            .caseUrl(getCaseUrl(caseData, applicationTypes))
            .applicationType(applicationNames)
            .applicant(applicantName)
            .build();
    }

    public FailedPBANotificationData getApplicantNotifyData(List<ApplicationType> applicationTypes,
                                                            CaseData caseData) {
        String applicationNames = applicationTypes.stream()
            .map(ApplicationType::getType).collect(Collectors.joining(", "));
        return FailedPBANotificationData.builder()
            .applicationType(applicationNames)
            .caseUrl(getCaseUrl(caseData, applicationTypes))
            .build();
    }

    private String getCaseUrl(CaseData caseData, List<ApplicationType> applicationTypes) {
        if (applicationTypes.contains(C110A_APPLICATION)) {
            return getCaseUrl(caseData.getId());
        }
        if (applicationTypes.contains(A50_PLACEMENT)) {
            return getCaseUrl(caseData.getId(), PLACEMENT);
        }

        return getCaseUrl(caseData.getId(), OTHER_APPLICATIONS);
    }
}
