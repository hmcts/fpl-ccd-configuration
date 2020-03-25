package uk.gov.hmcts.reform.fpl.service.representative;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepresentativeNotificationService {
    private final NotificationService notificationService;

    public void sendNotificationToRepresentatives(final EventData eventData,
                                                  final Map<String, Object> parameters,
                                                  final List<Representative> representatives,
                                                  final String templateId) {
        representatives.stream()
            .filter(representative -> isNotBlank(representative.getEmail()))
            .forEach(representative -> notificationService.sendEmail(
                templateId,
                representative.getEmail(),
                parameters,
                eventData.getReference()));
    }
}
