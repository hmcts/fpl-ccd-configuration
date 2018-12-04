package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

@Service
public class NotificationService {

    private final NotificationClient notificationClient;

    @Autowired
    public NotificationService(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    public void sendMail(String email, String template,
                         Map<String, String> parameters, String reference) throws NotificationClientException {

        notificationClient.sendEmail(template, email, parameters, reference);
    }
}
