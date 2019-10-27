package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
public class EmailNotificationService {

    private final NotificationClient notificationClient;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public EmailNotificationService(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    public void sendNotification(final String templateId,
                                 final String email,
                                 final Map<String, Object> parameters,
                                 final String reference) {
        logger.debug("Sending submission notification (with template id: {}) to {}", templateId, email);
        try {
            notificationClient.sendEmail(templateId, email, parameters, reference);
        } catch (NotificationClientException e) {
            logger.error("Failed to send submission notification (with template id: {}) to {}", templateId, email, e);
        }
    }

    public void sendNotification(final String templateId,
                                 final List<String> emails,
                                 final Map<String, Object> parameters,
                                 final String reference) {
        logger.debug("Sending submission notification (with template id: {}) to {}", templateId, emails);

        if (!isEmpty(emails)) {
            emails.stream().filter(StringUtils::isNotBlank).forEach(email ->
                sendNotification(templateId, email, parameters, reference));
        }
    }
}
