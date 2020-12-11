package uk.gov.hmcts.reform.fpl.service.email;

import uk.gov.service.notify.SendEmailResponse;

public interface NotificationResponsePostProcessor {

    void process(SendEmailResponse response);

}
