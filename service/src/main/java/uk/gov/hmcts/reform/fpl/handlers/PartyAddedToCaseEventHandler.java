package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.PartyAddedToCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.email.content.PartyAddedToCaseContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE_CHILD_NAME;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE_WITH_CHILD;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PartyAddedToCaseEventHandler {
    private final PartyAddedToCaseContentProvider contentProvider;
    private final RepresentativeNotificationService representativeNotificationService;
    private final RepresentativeService representativeService;
    private final FeatureToggleService toggleService;

    @EventListener
    public void notifyParties(final PartyAddedToCaseEvent event) {
        CaseData caseData = event.getCaseData();
        CaseData caseDataBefore = event.getCaseDataBefore();

        List<Representative> representativesServedByDigitalService = representativeService.getUpdatedRepresentatives(
            caseData.getRepresentatives(), caseDataBefore.getRepresentatives(), DIGITAL_SERVICE
        );
        List<Representative> representativesServedByEmail = representativeService.getUpdatedRepresentatives(
            caseData.getRepresentatives(), caseDataBefore.getRepresentatives(), EMAIL
        );

        String template = toggleService.isEldestChildLastNameEnabled()
                          ? PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE_CHILD_NAME
                          : PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;

        NotifyData servedByEmailParameters = contentProvider.getPartyAddedToCaseNotificationParameters(caseData, EMAIL);

        representativeNotificationService.sendToUpdatedRepresentatives(
            template, servedByEmailParameters, caseData, representativesServedByEmail
        );

        template = toggleService.isEldestChildLastNameEnabled()
                   ? PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE_WITH_CHILD
                   : PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE;

        NotifyData servedByDigitalServiceParameters = contentProvider.getPartyAddedToCaseNotificationParameters(
            caseData, DIGITAL_SERVICE
        );

        representativeNotificationService.sendToUpdatedRepresentatives(
            template, servedByDigitalServiceParameters, caseData, representativesServedByDigitalService
        );
    }
}
