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
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;
import uk.gov.hmcts.reform.fpl.service.ConfidentialDetailsService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NewNoticeOfHearingEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingUpdatedEventHandler {

    private final NewNoticeOfHearingEmailContentProvider newNoticeOfHearingEmailContentProvider;
    private final HearingBookingService hearingBookingService;
    private final ObjectMapper mapper;
    private final NotificationService notificationService;
    private final RepresentativeService representativeService;
    private final InboxLookupService inboxLookupService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;

    @Async
    @EventListener
    public void sendEmail(final HearingsUpdated event) {
        EventData eventData = new EventData(event);

        final CaseDetails caseDetails = mapper.convertValue(event.getCallbackRequest().getCaseDetails(), CaseDetails.class);
        final CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<HearingBooking>> hearings = hearingBookingService.getSelectedHearings(caseData);

        hearings.forEach(hearing -> {

            List<String> emailList = getEmailList(eventData, caseDetails, caseData);

            emailList.forEach(recipientEmail -> {
                notificationService.sendEmail(CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE, recipientEmail,
                    buildNotificationParameters(caseDetails, hearing.getValue()), "");
            });
        });

    }

    private List<String> getEmailList(EventData eventData, CaseDetails caseDetails, CaseData caseData) {
        List<String> emailList = new ArrayList<>();

        List<Representative> representatives = representativeService.getRepresentativesByServedPreference(
            caseData.getRepresentatives(), RepresentativeServingPreferences.EMAIL);
        representatives.stream()
            .filter(representative -> isNotBlank(representative.getEmail()))
            .forEach(representative -> emailList.add(representative.getEmail()));

        emailList.add(inboxLookupService.getNotificationRecipientEmail(caseDetails,
            eventData.getLocalAuthorityCode()));

        emailList.add(cafcassLookupConfiguration.getCafcass(eventData.getLocalAuthorityCode()).getEmail());
        return emailList;
    }

    private Map<String, Object> buildNotificationParameters(CaseDetails caseDetails, HearingBooking hearingBooking) {
        newNoticeOfHearingEmailContentProvider.buildNewNoticeOfHearingNotification(caseDetails, hearingBooking);
        return Map.of();
    }

}
