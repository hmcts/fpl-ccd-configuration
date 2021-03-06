package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FurtherEvidenceUploadedEventHandler {
    private final FurtherEvidenceNotificationService furtherEvidenceNotificationService;

    @EventListener
    public void handleDocumentUploadedEvent(final FurtherEvidenceUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();
        final UserDetails uploader = event.getInitiatedBy();

        final Set<String> recipients = new HashSet<>();

        if (event.isUploadedByLA()) {
            if (hasNewNonConfidentialDocuments(caseData.getFurtherEvidenceDocumentsLA(),
                caseDataBefore.getFurtherEvidenceDocumentsLA())) {
                recipients.addAll(furtherEvidenceNotificationService.getRepresentativeEmails(caseData));
            }
        } else {
            if (hasNewNonConfidentialDocuments(caseData.getFurtherEvidenceDocuments(),
                caseDataBefore.getFurtherEvidenceDocuments())) {
                recipients.addAll(furtherEvidenceNotificationService.getRepresentativeEmails(caseData));
                recipients.addAll(furtherEvidenceNotificationService.getLocalAuthoritySolicitorEmails(caseData));
            }
        }

        recipients.removeIf(email -> Objects.equals(email, uploader.getEmail()));

        if (isNotEmpty(recipients)) {
            furtherEvidenceNotificationService.sendNotification(caseData, recipients, uploader.getFullName());
        }
    }

    private static boolean hasNewNonConfidentialDocuments(List<Element<SupportingEvidenceBundle>> newEvidenceBundle,
                                                          List<Element<SupportingEvidenceBundle>> oldEvidenceBundle) {
        List<SupportingEvidenceBundle> oldEvidenceBundleUnwrapped = unwrapElements(oldEvidenceBundle);
        return unwrapElements(newEvidenceBundle).stream()
            .anyMatch(d -> oldEvidenceBundleUnwrapped.stream()
                .noneMatch(old -> old.getDocument().equals(d.getDocument()))
                && !d.isConfidentialDocument());
    }
}
