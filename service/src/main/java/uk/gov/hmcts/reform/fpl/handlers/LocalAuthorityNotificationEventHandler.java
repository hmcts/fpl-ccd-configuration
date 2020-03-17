package uk.gov.hmcts.reform.fpl.handlers;

import uk.gov.hmcts.reform.fpl.model.event.EventData;

import java.util.Map;

public interface LocalAuthorityNotificationEventHandler extends NotificationEventHandler {
    Map<String, Object> buildEmailTemplatePersonalisationForLocalAuthority(EventData eventData);

    String getEmailRecipientForLocalAuthority(EventData eventData);
}
