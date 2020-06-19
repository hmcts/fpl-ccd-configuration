package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderReadyForPartyReviewEvent;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_PARTY_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderReadyForPartyReviewEventHandler {
    private final RepresentativeNotificationService representativeNotificationService;
    private final CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    @EventListener
    public void sendEmailForCaseManagementOrderReadyForPartyReview(
        final CaseManagementOrderReadyForPartyReviewEvent event) {
        EventData eventData = new EventData(event);

        Map<String, Object> digitalRepresentativesParameters = caseManagementOrderEmailContentProvider
            .buildCMOPartyReviewParameters(eventData.getCaseDetails(), event.getDocumentContents(), DIGITAL_SERVICE);

        representativeNotificationService.sendToRepresentativesByServedPreference(DIGITAL_SERVICE,
            CMO_READY_FOR_PARTY_REVIEW_NOTIFICATION_TEMPLATE, digitalRepresentativesParameters, eventData);

        Map<String, Object> emailRepresentativesParameters = caseManagementOrderEmailContentProvider
            .buildCMOPartyReviewParameters(eventData.getCaseDetails(), event.getDocumentContents(), EMAIL);

        representativeNotificationService.sendToRepresentativesByServedPreference(EMAIL,
            CMO_READY_FOR_PARTY_REVIEW_NOTIFICATION_TEMPLATE, emailRepresentativesParameters, eventData);
    }
}
