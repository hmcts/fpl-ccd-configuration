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
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NewNoticeOfHearingEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingUpdatedEventHandler {

    private final NewNoticeOfHearingEmailContentProvider newNoticeOfHearingEmailContentProvider;
    private final HearingBookingService hearingBookingService;
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

        List<Element<HearingBooking>> hearings = hearingBookingService.getSelectedHearings(caseData);

        hearings.forEach(hearing -> {
            sendNotificationToLA(eventData, caseDetails, hearing.getValue());
            sendNotificationToCafcass(eventData, caseDetails, hearing.getValue());
            sendNotificationToRepresentatives(eventData, caseDetails, hearing.getValue());
        });

    }

    private void sendNotificationToLA(EventData eventData, CaseDetails caseDetails, HearingBooking hearing) {
        String email = inboxLookupService.getNotificationRecipientEmail(caseDetails,
            eventData.getLocalAuthorityCode());
        notificationService.sendEmail(NOTICE_OF_NEW_HEARING, email,
            buildNotificationParameters(caseDetails, hearing), eventData.getReference());
    }

    private void sendNotificationToCafcass(EventData eventData, CaseDetails caseDetails, HearingBooking hearing) {
        String email = cafcassLookupConfiguration.getCafcass(eventData.getLocalAuthorityCode()).getEmail();
        notificationService.sendEmail(NOTICE_OF_NEW_HEARING, email,
            buildNotificationParameters(caseDetails, hearing), eventData.getReference());
    }

    private void sendNotificationToRepresentatives(
        EventData eventData, CaseDetails caseDetails, HearingBooking hearing) {
        representativeNotificationService
            .sendToRepresentativesByServedPreference(RepresentativeServingPreferences.EMAIL, NOTICE_OF_NEW_HEARING,
            buildNotificationParameters(caseDetails, hearing), eventData);
    }

    private Map<String, Object> buildNotificationParameters(CaseDetails caseDetails, HearingBooking hearingBooking) {
        return newNoticeOfHearingEmailContentProvider
            .buildNewNoticeOfHearingNotification(caseDetails, hearingBooking).toMap(mapper);
    }
}
