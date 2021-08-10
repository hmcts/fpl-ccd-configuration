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
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.representativeSolicitors;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FurtherEvidenceUploadedEventHandler {
    private final FurtherEvidenceNotificationService furtherEvidenceNotificationService;
    private final UserService userService;

    @EventListener
    public void handleDocumentUploadedEvent(final FurtherEvidenceUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();
        final UserDetails uploader = event.getInitiatedBy();

        boolean uploadedByLA = event.isUploadedByLA();
        List<Element<SupportingEvidenceBundle>> newBundle = getEvidenceBundle(caseData, uploadedByLA);
        List<Element<SupportingEvidenceBundle>> oldBundle = getEvidenceBundle(caseDataBefore, uploadedByLA);
        List<String> newNonConfidentialDocuments = getNewNonConfidentialDocumentsNames(newBundle, oldBundle);

        final Set<String> recipients = new HashSet<>();

        if (!newNonConfidentialDocuments.isEmpty()) {
            recipients.addAll(furtherEvidenceNotificationService.getRepresentativeEmails(caseData));

            if (!uploadedByLA) {
                recipients.addAll(furtherEvidenceNotificationService.getLocalAuthoritySolicitorEmails(caseData));
            }
        }

        recipients.removeIf(email -> Objects.equals(email, uploader.getEmail()));

        if (isNotEmpty(recipients)) {
            furtherEvidenceNotificationService.sendNotification(caseData, recipients, uploader.getFullName(),
                newNonConfidentialDocuments);
        }
    }

    private List<String> getNewNonConfidentialDocumentsNames(List<Element<SupportingEvidenceBundle>> newBundle,
                                                             List<Element<SupportingEvidenceBundle>> oldBundle) {
        List<String> documentNames = new ArrayList<String>();

        unwrapElements(newBundle).forEach(newDoc -> {
            if (!newDoc.isConfidentialDocument() && !unwrapElements(oldBundle).contains(newDoc)) {
                documentNames.add(newDoc.getName());
            }
        });
        return documentNames;
    }

    private List<Element<SupportingEvidenceBundle>> getEvidenceBundle(CaseData caseData, boolean uploadedByLA) {
        if (uploadedByLA) {
            return caseData.getFurtherEvidenceDocumentsLA();
        } else if (isSolicitor(caseData.getId())) {
            return caseData.getFurtherEvidenceDocumentsSolicitor();
        } else {
            return caseData.getFurtherEvidenceDocuments();
        }
    }

    private boolean isSolicitor(Long id) {
        return userService.hasAnyCaseRoleFrom(representativeSolicitors(), id.toString());
    }
}
