package uk.gov.hmcts.reform.fpl.service.email;

import org.springframework.stereotype.Component;
import uk.gov.service.notify.SendEmailResponse;

@Component
public class NoOpNotificationResponsePostProcessor implements NotificationResponsePostProcessor {

    @Override
    public void process(SendEmailResponse response) {
        // Do Nothing
    }
}
