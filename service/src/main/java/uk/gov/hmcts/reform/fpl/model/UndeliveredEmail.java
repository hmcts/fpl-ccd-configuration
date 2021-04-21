package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Value;
import uk.gov.service.notify.Notification;

@Value
@Builder
public class UndeliveredEmail {
    String recipient;
    String subject;
    String reference;

    public static UndeliveredEmail fromNotification(Notification notification) {
        return UndeliveredEmail.builder()
            .recipient(notification.getEmailAddress().orElseThrow(() -> new IllegalArgumentException(
                "Cannot convert notification without email address into email")))
            .subject(notification.getSubject().orElseThrow(() -> new IllegalArgumentException(
                "Cannot convert notification without subject into email")))
            .reference(notification.getReference().orElseThrow(() -> new IllegalArgumentException(
                "Cannot convert notification without reference into email")))
            .build();
    }
}
