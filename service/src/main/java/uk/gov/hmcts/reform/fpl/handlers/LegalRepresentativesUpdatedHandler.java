package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.LegalRepresentativesUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentativesChange;
import uk.gov.hmcts.reform.fpl.service.LegalRepresentativesDifferenceCalculator;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.LegalRepresentativeAddedContentProvider;

import java.util.Set;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LegalRepresentativesUpdatedHandler {
    private final LegalRepresentativeAddedContentProvider legalRepresentativeAddedContentProvider;
    private final LegalRepresentativesDifferenceCalculator differenceCalculator;
    private final NotificationService notificationService;

    @EventListener
    public void sendEmailToLegalRepresentativesAddedToCase(final LegalRepresentativesUpdated event) {
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
    }
}
