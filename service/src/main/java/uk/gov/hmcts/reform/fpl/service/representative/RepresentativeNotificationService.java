package uk.gov.hmcts.reform.fpl.service.representative;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepresentativeNotificationService {

    private final NotificationService notificationService;

    public void sendToRepresentativesByServedPreference(final RepresentativeServingPreferences servedPreference,
                                                        final String templateId,
                                                        final NotifyData notifyData,
                                                        final CaseData caseData) {
        List<Representative> representatives = caseData.getRepresentativesByServedPreference(servedPreference);

        if (!representatives.isEmpty()) {
            sendNotificationToRepresentatives(caseData.getId(), notifyData, representatives, templateId);
        }
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
}
