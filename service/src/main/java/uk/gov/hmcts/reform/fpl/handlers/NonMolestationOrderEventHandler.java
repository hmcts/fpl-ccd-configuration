package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.order.NonMolestationOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NON_MOLESTATION_ORDER_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElements;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NonMolestationOrderEventHandler {
    private final TranslationRequestService translationRequestService;
    private final OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final RepresentativesInbox representativesInbox;
    private final NotificationService notificationService;

    @EventListener
    public void notifyTranslationTeam(NonMolestationOrderEvent event) {
        translationRequestService.sendRequest(event.getCaseData(),
            Optional.of(event.getLanguageTranslationRequirement()),
            event.getOrderDocument(), event.getOrderTitle());
    }

    @EventListener
    public void notifyParties(NonMolestationOrderEvent orderEvent) {
        final ManageOrdersEventData eventData = orderEvent.getEventData();
        final CaseData caseData = orderEvent.getCaseData();
        final DocumentReference orderDocument = orderEvent.getOrderDocument();
        final String applicantSelected = eventData.getManageOrdersNonMolestationOrderApplicant().getValueCode();

        Set<String> recipient = new HashSet<>();

        if ("applicant".equals(applicantSelected)) {
            recipient.addAll(localAuthorityRecipients.getRecipients(RecipientsRequest.builder()
                .caseData(caseData)
                .secondaryLocalAuthorityExcluded(true)
                .legalRepresentativesExcluded(true)
                .build()));
        } else if ("secondaryLocalAuthority".equals(applicantSelected)) {
            recipient.addAll(localAuthorityRecipients.getRecipients(RecipientsRequest.builder()
                .caseData(caseData)
                .designatedLocalAuthorityExcluded(true)
                .legalRepresentativesExcluded(true)
                .build()));
        } else {
            UUID uuid = UUID.fromString(applicantSelected);
            List<Element<Respondent>> respondentSelected =
                findElement(uuid, caseData.getAllRespondents()).stream().toList();

            if (!respondentSelected.isEmpty()) {
                recipient.addAll(representativesInbox.getRespondentSolicitorEmailsFromList(respondentSelected,
                    RepresentativeServingPreferences.DIGITAL_SERVICE));
                recipient.addAll(representativesInbox.getRespondentSolicitorEmailsFromList(respondentSelected,
                    RepresentativeServingPreferences.EMAIL));
            } else {
                List<Element<Child>> childSelected = findElement(uuid, caseData.getAllChildren()).stream().toList();
                recipient.addAll(representativesInbox.getChildrenSolicitorEmailsFromList(childSelected,
                    RepresentativeServingPreferences.DIGITAL_SERVICE));
                recipient.addAll(representativesInbox.getChildrenSolicitorEmailsFromList(childSelected,
                    RepresentativeServingPreferences.EMAIL));
            }
        }

        if (recipient.isEmpty()) {
            log.warn("No valid recipient for Non-molestation order");
        } else {
            final NotifyData notifyData = orderIssuedEmailContentProvider.getNotifyDataWithCaseUrl(caseData,
                orderDocument, GENERATED_ORDER);

            notificationService.sendEmail(NON_MOLESTATION_ORDER_NOTIFICATION_TEMPLATE, recipient, notifyData,
                caseData.getId());
        }
    }
}
