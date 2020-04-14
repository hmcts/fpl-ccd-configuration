package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.PartyAddedToCaseEvent;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.service.email.content.PartyAddedToCaseContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PartyAddedToCaseEventHandler {
    private final PartyAddedToCaseContentProvider partyAddedToCaseContentProvider;
    private final RepresentativeNotificationService representativeNotificationService;

    @EventListener
    public void sendEmailToPartiesAddedToCase(final PartyAddedToCaseEvent event) {
        EventData eventData = new EventData(event);
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();

        Map<String, Object> servedByEmailParameters = partyAddedToCaseContentProvider
            .getPartyAddedToCaseNotificationParameters(caseDetails, EMAIL);

        representativeNotificationService.sendToRepresentativesByServedPreference(EMAIL,
            PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE, servedByEmailParameters, eventData);

        Map<String, Object> servedByDigitalServiceParameters = partyAddedToCaseContentProvider
            .getPartyAddedToCaseNotificationParameters(caseDetails, DIGITAL_SERVICE);

        representativeNotificationService.sendToRepresentativesByServedPreference(DIGITAL_SERVICE,
            PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE, servedByDigitalServiceParameters,
            eventData);
    }
}
