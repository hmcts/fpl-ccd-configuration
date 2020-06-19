package uk.gov.hmcts.reform.fpl.service.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationService {
    private final NotificationClient notificationClient;
    private final ObjectMapper mapper;

    public void sendEmail(String templateId, String email, Map<String, Object> parameters, String reference) {
        log.debug("Sending email (with template id: {}) to {}", templateId, email);
        try {
            notificationClient.sendEmail(templateId, email, parameters, reference);
        } catch (NotificationClientException e) {
            log.error("Failed to send email (with template id: {}) to {}", templateId, email, e);
        }
    }

    public void sendEmail(String templateId, String email, NotifyData data, String reference) {
        sendEmail(templateId, email, data.toMap(mapper), reference);
    }
}
