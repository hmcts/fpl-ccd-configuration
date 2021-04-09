package uk.gov.hmcts.reform.fpl.service.email;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Collection;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.MaskHelper.maskEmail;

@Slf4j
@Service
public class NotificationService {
    public static final String SEPARATOR = "/";

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

    public void sendEmail(String templateId, String recipient, NotifyData data, String reference) {
        Map<String, Object> personalisation = mapper.convertValue(data, new TypeReference<>() {
        });
        log.debug("Sending email (with template id: {}) to {}", templateId, maskEmail(recipient));
        try {
            notificationClient.sendEmail(templateId, recipient, personalisation, environment + SEPARATOR + reference);
        } catch (NotificationClientException e) {
            log.error("Failed to send email (with template id: {}) to {}", templateId, maskEmail(recipient), e);
        }
    }

    public void sendEmail(String templateId, Collection<String> emails, NotifyData data, String reference) {
        emails.forEach(email -> sendEmail(templateId, email, data, reference));
    }

    public void sendEmail(String templateId, String recipient, NotifyData data, Long reference) {
        sendEmail(templateId, recipient, data, reference.toString());
    }
}
