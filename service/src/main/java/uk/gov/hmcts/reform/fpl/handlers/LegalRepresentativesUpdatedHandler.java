package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.LegalRepresentativesUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentativesChange;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.LegalRepresentativesDifferenceCalculator;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.LegalCounsellorEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.LegalRepresentativeAddedContentProvider;

import java.util.Set;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_COUNSELLOR_REMOVED_THEMSELVES;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LegalRepresentativesUpdatedHandler {
    private final LegalRepresentativeAddedContentProvider legalRepresentativeAddedContentProvider;
    private final LegalRepresentativesDifferenceCalculator differenceCalculator;
    private final NotificationService notificationService;
    private final UserService userService;
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final LegalCounsellorEmailContentProvider legalCounsellorEmailContentProvider;

    @EventListener
    public void sendEmailToLegalRepresentativesUpdated(final LegalRepresentativesUpdated event) {
        CaseData caseData = event.getCaseData();
        CaseData caseDataBefore = event.getCaseDataBefore();

        LegalRepresentativesChange legalRepresentativesChange = differenceCalculator.calculate(
            unwrapElements(caseDataBefore.getLegalRepresentatives()),
            unwrapElements(caseData.getLegalRepresentatives())
        );

        Set<LegalRepresentative> added = legalRepresentativesChange.getAdded();

        added.forEach(legalRepresentative -> notificationService.sendEmail(
            LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE,
            legalRepresentative.getEmail(),
            legalRepresentativeAddedContentProvider.getNotifyData(legalRepresentative, caseData),
            caseData.getId()
        ));

        final String currentUserEmail = userService.getUserEmail();
        legalRepresentativesChange.getRemoved().stream()
            .filter(legalRepresentative ->
                StringUtils.equalsIgnoreCase(currentUserEmail, legalRepresentative.getEmail()))
            .findFirst()
            // notify LA if legal representative has removed themselves
            .ifPresent(legalRepresentative ->
                localAuthorityRecipients.getRecipients(RecipientsRequest.builder()
                        .designatedLocalAuthorityExcluded(false)
                        .secondaryLocalAuthorityExcluded(false)
                        .legalRepresentativesExcluded(true)
                        .caseData(caseData)
                        .build())
                    .forEach(laEmail ->
                        notificationService.sendEmail(LEGAL_COUNSELLOR_REMOVED_THEMSELVES, laEmail,
                            legalCounsellorEmailContentProvider
                                .buildLegalCounsellorRemovedThemselvesNotificationTemplate(caseData,
                                    legalRepresentative.getFullName()),
                            caseData.getId())));
    }
}
