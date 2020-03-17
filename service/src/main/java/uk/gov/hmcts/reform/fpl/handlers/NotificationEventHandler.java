package uk.gov.hmcts.reform.fpl.handlers;

import uk.gov.hmcts.reform.fpl.events.CallbackEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.EventData;

/**
 * All gov dot notify events do four things once event triggered:.
 * - get EventData
 * - from EventData, build email personalization
 * - get email recipients from {@link CaseData}
 * - send notification
 */
public interface NotificationEventHandler {
    default EventData getEventData(final CallbackEvent event) {
        return new EventData(event);
    }
}
