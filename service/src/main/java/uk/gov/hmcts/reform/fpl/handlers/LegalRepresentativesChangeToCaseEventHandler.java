package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.LegalRepresentativesChangeToCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentativesChange;
import uk.gov.hmcts.reform.fpl.service.LegalRepresentativesDifferenceCalculator;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.LegalRepresentativeAddedContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LegalRepresentativesChangeToCaseEventHandler {
    private final LegalRepresentativeAddedContentProvider legalRepresentativeAddedContentProvider;
    private final LegalRepresentativesDifferenceCalculator legalRepresentativesDifferenceCalculator;
    private final NotificationService notificationService;


    @EventListener
    public void sendEmailToLegalRepresentativesAddedToCase(final LegalRepresentativesChangeToCaseEvent event) {
        CaseData caseData = event.getCaseData();
        CaseData caseDataBefore = event.getCaseDataBefore();

        LegalRepresentativesChange legalRepresentativesChange = legalRepresentativesDifferenceCalculator.calculate(
            unwrapElements(caseDataBefore.getLegalRepresentatives()),
            unwrapElements(caseData.getLegalRepresentatives())
        );

        legalRepresentativesChange.getAdded().forEach(
            legalRepresentative -> notificationService.sendEmail(
                LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE,
                legalRepresentative.getEmail(),
                legalRepresentativeAddedContentProvider.getParameters(legalRepresentative,caseData),
                caseData.getId().toString())
        );
    }
}
