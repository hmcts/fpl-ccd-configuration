package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.PartyAddedToCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.email.content.PartyAddedToCaseContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PartyAddedToCaseEventHandler {
    private final ObjectMapper objectMapper;
    private final RepresentativeService representativeService;
    private final PartyAddedToCaseContentProvider partyAddedToCaseContentProvider;
    private final RepresentativeNotificationService representativeNotificationService;

    @EventListener
    public void sendEmailToPartiesAddedToCase(final PartyAddedToCaseEvent event) {
        EventData eventData = new EventData(event);
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();

        Map<String, Object> servedByEmailParameters = partyAddedToCaseContentProvider
            .getPartyAddedToCaseNotificationParameters(caseDetails, EMAIL);
        Map<String, Object> servedByDigitalServiceParameters = partyAddedToCaseContentProvider
            .getPartyAddedToCaseNotificationParameters(caseDetails, DIGITAL_SERVICE);

        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        CaseData caseDataBefore = objectMapper.convertValue(event.getCallbackRequest().getCaseDetailsBefore().getData(),
            CaseData.class);

        List<Representative> representativesServedByDigitalService = representativeService.getUpdatedRepresentatives(
            caseData.getRepresentatives(), caseDataBefore.getRepresentatives(), DIGITAL_SERVICE);
        List<Representative> representativesServedByEmail = representativeService.getUpdatedRepresentatives(
            caseData.getRepresentatives(), caseDataBefore.getRepresentatives(), EMAIL);

        representativeNotificationService.sendNotificationToRepresentatives(eventData, servedByEmailParameters,
            representativesServedByEmail, PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE);
        representativeNotificationService.sendNotificationToRepresentatives(eventData, servedByDigitalServiceParameters,
            representativesServedByDigitalService, PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE);
    }
}
