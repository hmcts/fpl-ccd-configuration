package uk.gov.hmcts.reform.fpl.service.representative;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;

import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepresentativeNotificationService {

    private final NotificationService notificationService;
    private final RepresentativesInbox representativesInbox;

    public void sendToRepresentativesByServedPreference(final RepresentativeServingPreferences servedPreference,
                                                        final String templateId,
                                                        final NotifyData notifyData,
                                                        final CaseData caseData) {
        if (servedPreference.equals(RepresentativeServingPreferences.POST)) {
            throw new IllegalArgumentException("Preference should not be POST");
        }
        Set<String> emailRepresentatives = representativesInbox.getEmailsByPreference(caseData, servedPreference);
        sendNotificationToRepresentatives(caseData.getId(), notifyData, emailRepresentatives, templateId);
    }

    public void sendToUpdatedRepresentatives(final String templateId,
                                             final NotifyData notifyData,
                                             final CaseData caseData,
                                             List<Representative> representatives) {

        if (!representatives.isEmpty()) {
            sendNotificationToRepresentatives(caseData.getId(), notifyData, representatives, templateId);
        }
    }

    public void sendNotificationToRepresentatives(final Long caseId,
                                                  final NotifyData parameters,
                                                  final List<Representative> representatives,
                                                  final String templateId) {
        representatives.stream()
            .filter(representative -> isNotBlank(representative.getEmail()))
            .forEach(representative -> notificationService.sendEmail(
                templateId,
                representative.getEmail(),
                parameters,
                caseId));
    }

    public void sendNotificationToRepresentatives(final Long caseId,
                                                  final NotifyData parameters,
                                                  final Set<String> representatives,
                                                  final String templateId) {
        representatives.stream()
            .forEach(representative -> notificationService.sendEmail(
                templateId,
                representative,
                parameters,
                caseId));
    }
}
