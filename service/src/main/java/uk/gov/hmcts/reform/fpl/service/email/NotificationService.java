package uk.gov.hmcts.reform.fpl.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationService {
    private final NotificationClient notificationClient;

    public void sendEmail(String templateId, String email, Map<String, Object> parameters, String reference) {
        log.debug("Sending email (with template id: {}) to {}", templateId, email);
        try {
            SendEmailResponse response = notificationClient.sendEmail(templateId, email, parameters, reference);
            System.out.println(response.getBody() + email);
            notificationClient.sendEmail(templateId, email, parameters, reference);
        } catch (NotificationClientException e) {
            log.error("Failed to send email (with template id: {}) to {}", templateId, email, e);
        }
    }
}
