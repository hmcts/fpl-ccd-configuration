package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.SOLICITOR;
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

        DocumentUploadNotificationUserType userType = event.getUserType();
        List<String> newNonConfidentialDocuments = getNewNonConfidentialBundleNames(caseData, caseDataBefore, userType);

        final Set<String> recipients = new HashSet<>();

        if (!newNonConfidentialDocuments.isEmpty()) {
            recipients.addAll(furtherEvidenceNotificationService.getRepresentativeEmails(caseData));

            if (userType != LOCAL_AUTHORITY) {
                recipients.addAll(furtherEvidenceNotificationService.getLocalAuthoritySolicitorEmails(caseData));
            }
        }

        recipients.removeIf(email -> Objects.equals(email, uploader.getEmail()));

        if (isNotEmpty(recipients)) {
            furtherEvidenceNotificationService.sendNotification(caseData, recipients, uploader.getFullName(),
                newNonConfidentialDocuments);
        }
    }


    private List<String> getNewNonConfidentialBundleNames(CaseData caseData, CaseData caseDataBefore,
                                                          DocumentUploadNotificationUserType userType) {

        var newBundle = getEvidenceBundle(caseData, userType);
        var oldBundle = getEvidenceBundle(caseDataBefore, userType);

        List<String> documentNames = new ArrayList<String>();

        unwrapElements(newBundle).forEach(newDoc -> {
            if (!newDoc.isConfidentialDocument() && !unwrapElements(oldBundle).contains(newDoc)) {
                documentNames.add(newDoc.getName());
            }
        });
        return documentNames;
    }

    private List<Element<SupportingEvidenceBundle>> getEvidenceBundle(CaseData caseData,
                                                                      DocumentUploadNotificationUserType userType) {
        if (userType == LOCAL_AUTHORITY) {
            return caseData.getFurtherEvidenceDocumentsLA();
        } else if (userType == SOLICITOR) {
            List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle =
                defaultIfNull(caseData.getFurtherEvidenceDocumentsSolicitor(), List.of());
            List<Element<SupportingEvidenceBundle>> respondentStatementsBundle =
                getEvidenceBundleFromRespondentStatements(caseData);

            return concatEvidenceBundles(furtherEvidenceBundle, respondentStatementsBundle);
        } else {
            return caseData.getFurtherEvidenceDocuments();
        }
    }

    private List<Element<SupportingEvidenceBundle>> getEvidenceBundleFromRespondentStatements(CaseData caseData) {
        List<Element<SupportingEvidenceBundle>> evidenceBundle = new ArrayList<>();
        caseData.getRespondentStatements().forEach(statement -> {
            evidenceBundle.addAll(statement.getValue().getSupportingEvidenceBundle());
        });
        return evidenceBundle;
    }

    private List<Element<SupportingEvidenceBundle>> concatEvidenceBundles(List<Element<SupportingEvidenceBundle>> b1,
                                                                          List<Element<SupportingEvidenceBundle>> b2) {
            return Stream.concat(b1.stream(), b2.stream()).collect(Collectors.toList());
    }
}
