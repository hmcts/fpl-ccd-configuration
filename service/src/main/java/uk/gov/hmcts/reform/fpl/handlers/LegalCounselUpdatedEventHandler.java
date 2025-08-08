package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorAdded;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.LegalCounsellorEmailContentProvider;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_COUNSELLOR_ADDED_EMAIL_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_COUNSELLOR_REMOVED_EMAIL_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_COUNSELLOR_REMOVED_THEMSELVES;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_COUNSELLOR_SELF_REMOVED_EMAIL_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.BARRISTER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor
public class LegalCounselUpdatedEventHandler {

    private final CaseAccessService caseAccessService;
    private final NotificationService notificationService;
    private final LegalCounsellorEmailContentProvider contentProvider;
    private final UserService userService;

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

        final String currentUserEmail = userService.getUserEmail();
        // legal counsellor removed themselves from the case
        if (StringUtils.equalsIgnoreCase(legalCounsellor.getEmail(), currentUserEmail)) {
            notificationService.sendEmail(
                LEGAL_COUNSELLOR_SELF_REMOVED_EMAIL_TEMPLATE, legalCounsellor.getEmail(),
                contentProvider.buildLegalCounsellorRemovedNotificationTemplate(caseData, event), caseId);

            // Send notification to the solicitor that legal counsellor was acting for
            Set<String> solicitorEmails =
                Stream.concat(unwrapElements(caseData.getRespondents1()).stream(),
                        unwrapElements(caseData.getChildren1()).stream())
                    .filter(withSolicitor ->
                        unwrapElements(withSolicitor.getLegalCounsellors()).stream()
                            .map(LegalCounsellor::getEmail)
                            .anyMatch(currentUserEmail::equals))
                    .map(withSolicitor -> withSolicitor.getSolicitor().getEmail())
                    .collect(Collectors.toSet());

            for (String emailToBeSent : solicitorEmails) {
                notificationService.sendEmail(
                    LEGAL_COUNSELLOR_REMOVED_THEMSELVES, emailToBeSent,
                    contentProvider.buildLegalCounsellorRemovedThemselvesNotificationTemplate(caseData,
                        legalCounsellor.getFullName()),
                    caseId
                );
            }
        } else {
            notificationService.sendEmail(
                LEGAL_COUNSELLOR_REMOVED_EMAIL_TEMPLATE, legalCounsellor.getEmail(),
                contentProvider.buildLegalCounsellorRemovedNotificationTemplate(caseData, event), caseId
            );
        }
    }
}
