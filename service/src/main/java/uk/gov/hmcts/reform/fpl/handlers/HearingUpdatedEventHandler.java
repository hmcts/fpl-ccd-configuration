package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.HearingsUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NewNoticeOfHearingTemplate;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NewNoticeOfHearingEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingUpdatedEventHandler {

    private final NewNoticeOfHearingEmailContentProvider newNoticeOfHearingEmailContentProvider;
    private final ObjectMapper mapper;
    private final NotificationService notificationService;
    private final RepresentativeNotificationService representativeNotificationService;
    private final InboxLookupService inboxLookupService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;

    @Async
    @EventListener
    public void sendEmail(final HearingsUpdated event) {
        EventData eventData = new EventData(event);

        final CaseDetails caseDetails = mapper
            .convertValue(event.getCallbackRequest().getCaseDetails(), CaseDetails.class);
        final CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        caseData.getSelectedHearings().forEach(hearing -> {
            NewNoticeOfHearingTemplate params = buildNotificationParameters(caseDetails, hearing.getValue());

            sendNotificationToLA(eventData, caseDetails, params);
            sendNotificationToCafcass(eventData, params);
            sendNotificationToRepresentatives(eventData, params);
        });

    }

    private void sendNotificationToLA(EventData eventData, CaseDetails caseDetails, NewNoticeOfHearingTemplate params) {
        String email = inboxLookupService.getNotificationRecipientEmail(caseDetails,
            eventData.getLocalAuthorityCode());
        notificationService.sendEmail(NOTICE_OF_NEW_HEARING, email,
            params, eventData.getReference());
    }

    private void sendNotificationToCafcass(EventData eventData, NewNoticeOfHearingTemplate params) {
        String email = cafcassLookupConfiguration.getCafcass(eventData.getLocalAuthorityCode()).getEmail();
        notificationService.sendEmail(NOTICE_OF_NEW_HEARING, email,
            params, eventData.getReference());
    }

    private void sendNotificationToRepresentatives(
        EventData eventData, NewNoticeOfHearingTemplate params) {
        representativeNotificationService
            .sendToRepresentativesByServedPreference(RepresentativeServingPreferences.EMAIL, NOTICE_OF_NEW_HEARING,
                params.toMap(mapper), eventData);
    }

    private NewNoticeOfHearingTemplate buildNotificationParameters(CaseDetails caseDetails,
                                                                   HearingBooking hearingBooking) {
        return newNoticeOfHearingEmailContentProvider
            .buildNewNoticeOfHearingNotification(caseDetails, hearingBooking,
                RepresentativeServingPreferences.DIGITAL_SERVICE);
    }
}
