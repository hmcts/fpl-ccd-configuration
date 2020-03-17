package uk.gov.hmcts.reform.fpl.handlers;

import uk.gov.hmcts.reform.fpl.model.event.EventData;

import java.util.Map;

public interface CafcassNotificationEventHandler extends NotificationEventHandler {
    String getEmailRecipientForCafcass(String localAuthorityCode);

    Map<String, Object> buildEmailTemplatePersonalisationForCafcass(EventData eventData);
}
