package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.furtherevidence.FurtherEvidenceDocumentUploadedData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FurtherEvidenceUploadedEmailContentProvider;


import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FurtherEvidenceUploadedEventHandler {

    private final OrganisationService organisationService;
    private final FurtherEvidenceUploadedEmailContentProvider furtherEvidenceUploadedEmailContentProvider;
    private final NotificationService notificationService;
    private final InboxLookupService inboxLookupService;

    @EventListener
    public void notifyRepresentatives(final FurtherEvidenceUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        switch(event.getUploadedBy()) {
            case "LA_SOLICITOR":
                notifySolicitors(caseData);
                break;
            case "SOLICITOR":
                notifyLASolicitors(caseData);
                break;
            case "HMCTS_USER":
                notifyLASolicitors(caseData);
                notifySolicitors(caseData);
                break;
            default:
                log.error("Further evidence uploaded by unknown user type {}", event.getUploadedBy());
                break;
        }
    }

    private void notifyLASolicitors(final CaseData caseData) {
        Set<String> recipients = inboxLookupService.getRecipients(LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());


        notificationService.sendEmail(
            FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE, recipients,
            furtherEvidenceUploadedEmailContentProvider.buildParameters(caseData), caseData.getId().toString());
    }

    private void notifySolicitors(final CaseData caseData) {
        List<Element<Respondent>> respondents1 = caseData.getRespondents1();

    }
}
