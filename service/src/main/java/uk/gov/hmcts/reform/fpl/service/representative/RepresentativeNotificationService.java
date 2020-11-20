package uk.gov.hmcts.reform.fpl.service.representative;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepresentativeNotificationService {

    private final NotificationService notificationService;
    private final RepresentativeService representativeService;

    public void sendToRepresentativesByServedPreference(final RepresentativeServingPreferences servedPreference,
                                                        final String templateId,
                                                        final Map<String, Object> templateParameters,
                                                        final CaseData caseData) {
        List<Representative> representatives = representativeService.getRepresentativesByServedPreference(
            caseData.getRepresentatives(), servedPreference);

        if (!representatives.isEmpty()) {
            sendNotificationToRepresentatives(caseData.getId().toString(), templateParameters, representatives,
                templateId);
        }
    }

    public void sendToUpdatedRepresentatives(final String templateId,
                                             final Map<String, Object> templateParameters,
                                             final CaseData caseData,
                                             List<Representative> representatives) {

        if (!representatives.isEmpty()) {
            sendNotificationToRepresentatives(caseData.getId().toString(), templateParameters, representatives,
                templateId);
        }
    }

    private void sendNotificationToRepresentatives(final String caseId,
                                                   final Map<String, Object> parameters,
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
