package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FurtherEvidenceUploadedEmailContentProvider;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FurtherEvidenceNotificationService {
    private final InboxLookupService inboxLookupService;
    private final NotificationService notificationService;
    private final FurtherEvidenceUploadedEmailContentProvider furtherEvidenceUploadedEmailContentProvider;

    public Set<String> getLocalAuthoritySolicitorEmails(CaseData caseData) {
        return inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());
    }

    public Set<String> getRespondentRepresentativeEmails(CaseData caseData) {
        List<Element<Representative>> representatives = nullSafeList(caseData.getRepresentatives());
        List<Respondent> respondents = unwrapElements(caseData.getRespondents1());

        return representatives.stream().filter(r -> respondents.stream().anyMatch(
            respondent -> unwrapElements(respondent.getRepresentedBy()).stream().anyMatch(rep -> rep == r.getId())))
            .map(r -> r.getValue().getEmail())
            .collect(Collectors.toSet());
    }

    public void sendFurtherEvidenceDocumentsUploadedNotification(CaseData caseData, Set<String> recipients, String sender) {
        if (!recipients.isEmpty()) {
            notificationService.sendEmail(FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE, recipients,
                furtherEvidenceUploadedEmailContentProvider.buildParameters(caseData, sender), caseData.getId().toString());
        }
    }
}
