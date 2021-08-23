package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorAdded;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorRemoved;
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
    private final LegalCounsellorEmailContentProvider contentProvider;

    @EventListener
    public void handleLegalCounsellorAddedEvent(LegalCounsellorAdded event) {
        CaseData caseData = event.getCaseData();
        Long caseId = caseData.getId();
        LegalCounsellor legalCounsellor = event.getLegalCounsellor();
        String userId = legalCounsellor.getUserId();

        caseAccessService.grantCaseRoleToUser(caseId, userId, BARRISTER);
        notificationService.sendEmail(
            LEGAL_COUNSELLOR_ADDED_EMAIL_TEMPLATE, legalCounsellor.getEmail(),
            contentProvider.buildLegalCounsellorAddedNotificationTemplate(caseData), caseId
        );
    }

    @EventListener
    public void handleLegalCounsellorRemovedEvent(LegalCounsellorRemoved event) {
        CaseData caseData = event.getCaseData();
        Long caseId = caseData.getId();
        LegalCounsellor legalCounsellor = event.getLegalCounsellor();
        String userId = legalCounsellor.getUserId();

        caseAccessService.revokeCaseRoleFromUser(caseId, userId, BARRISTER);
        notificationService.sendEmail(
            LEGAL_COUNSELLOR_REMOVED_EMAIL_TEMPLATE, legalCounsellor.getEmail(),
            contentProvider.buildLegalCounsellorRemovedNotificationTemplate(caseData, event), caseId
        );
    }
}
