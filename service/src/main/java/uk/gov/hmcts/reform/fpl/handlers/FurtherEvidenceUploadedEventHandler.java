package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FurtherEvidenceUploadedEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FurtherEvidenceUploadedEventHandler {
    private final FurtherEvidenceUploadedEmailContentProvider furtherEvidenceUploadedEmailContentProvider;
    private final NotificationService notificationService;
    private final InboxLookupService inboxLookupService;

    @EventListener
    public void notifyRepresentatives(final FurtherEvidenceUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        final String excludedEmail = event.getInitiatedBy().getEmail();

        switch(event.getUploadedBy()) {
            case "LA_SOLICITOR":
                notifySolicitors(caseData, excludedEmail);
                break;
            case "SOLICITOR":
            case "HMCTS_USER":
                notifyLASolicitors(caseData, excludedEmail);
                notifySolicitors(caseData, excludedEmail);
                break;
            default:
                log.error("Further evidence uploaded by unknown user type {}", event.getUploadedBy());
                break;
        }
    }

    private void notifyLASolicitors(final CaseData caseData, final String excludeEmail) {
        Set<String> recipients = inboxLookupService.getRecipients(LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());
        if(!StringUtils.isEmpty(excludeEmail))
        {
           recipients = recipients.stream().filter( r -> r.equals(excludeEmail)).collect(Collectors.toSet());
        }
        notificationService.sendEmail(
            FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE, recipients,
            furtherEvidenceUploadedEmailContentProvider.buildParameters(caseData), caseData.getId().toString());
    }

    private void notifySolicitors(final CaseData caseData, final String  excludeEmail) {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments = caseData.getFurtherEvidenceDocuments();

        List<Element<SupportingEvidenceBundle>> nonConfidentialDocuments = furtherEvidenceDocuments.stream().filter(doc -> !doc.getValue().isConfidentialDocument()).collect(Collectors.toList());

        if(!nonConfidentialDocuments.isEmpty()) {

            caseData.getAllRespondents().forEach( r -> r.getValue().getParty().getPartyId());
            Set<String> recipients =
                caseData.getAllRespondents().stream().filter(Objects::nonNull).map(this::getRespondentEmail).collect(Collectors.toSet());

            if(!StringUtils.isEmpty(excludeEmail))
            {
                recipients = recipients.stream().filter( r -> r.equals(excludeEmail)).collect(Collectors.toSet());
            }

            notificationService.sendEmail(FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE, recipients,
                furtherEvidenceUploadedEmailContentProvider.buildParameters(caseData), caseData.getId().toString());
        }
    }

    private String getRespondentEmail(Element<Respondent> respondent) {
        //TODO: Get rid of this helper and find a more reusable or inline implementation
        return respondent.getValue().getParty().getEmail().getEmail();
    }
}
