package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.LegalCounsellorAdded;
import uk.gov.hmcts.reform.fpl.events.LegalCounsellorRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.LegalCounsellorEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_COUNSELLOR_ADDED_EMAIL_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_COUNSELLOR_REMOVED_EMAIL_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.BARRISTER;

@Component
@RequiredArgsConstructor
public class LegalCounselUpdatedEventHandler {

    private final CaseAccessService caseAccessService;
    private final NotificationService notificationService;
    private final LegalCounsellorEmailContentProvider emailContentProvider;

    @EventListener
    public void handleLegalCounsellorAddedEvent(LegalCounsellorAdded event) {
        CaseData caseData = event.getCaseData();
        Long caseId = caseData.getId();
        String userId = event.getLegalCounsellor().getKey();
        LegalCounsellor legalCounsellor = event.getLegalCounsellor().getValue();

        caseAccessService.grantCaseRoleToUser(caseId, userId, BARRISTER);
        notificationService.sendEmail(LEGAL_COUNSELLOR_ADDED_EMAIL_TEMPLATE,
            legalCounsellor.getEmail(),
            emailContentProvider.buildLegalCounsellorAddedNotificationTemplate(caseData),
            caseId);
    }

    @EventListener
    public void handleLegalCounsellorRemovedEvent(LegalCounsellorRemoved event) {
        CaseData caseData = event.getCaseData();
        Long caseId = caseData.getId();
        String userId = event.getLegalCounsellor().getKey();
        LegalCounsellor legalCounsellor = event.getLegalCounsellor().getValue();

        caseAccessService.revokeCaseRoleFromUser(caseId, userId, BARRISTER);
        notificationService.sendEmail(LEGAL_COUNSELLOR_REMOVED_EMAIL_TEMPLATE,
            legalCounsellor.getEmail(),
            emailContentProvider.buildLegalCounsellorRemovedNotificationTemplate(
                caseData, event
            ),
            caseId);
    }

}
