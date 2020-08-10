package uk.gov.hmcts.reform.fpl.service.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

@Slf4j
@Service
public class NotificationService {
    private final NotificationClient notificationClient;
    private final ObjectMapper mapper;
    private final String environment;

    @Autowired
    public NotificationService(NotificationClient notificationClient,
                               ObjectMapper mapper,
                               @Value("${fpl.env}") String environment) {
        this.notificationClient = notificationClient;
        this.mapper = mapper;
        this.environment = environment;
    }

    public void sendEmail(String templateId, String email, Map<String, Object> parameters, String reference) {
        log.debug("Sending email (with template id: {}) to {}", templateId, email);
        try {
            notificationClient.sendEmail(templateId, email, parameters, environment + "/" + reference);
        } catch (NotificationClientException e) {
            log.error("Failed to send email (with template id: {}) to {}", templateId, email, e);
        }
    }

    public void sendEmail(String templateId, String email, NotifyData data, String reference) {
        sendEmail(templateId, email, data.toMap(mapper), reference);
    }
}
