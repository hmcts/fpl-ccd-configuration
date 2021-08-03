package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.LegalCounsellorAdded;
import uk.gov.hmcts.reform.fpl.events.LegalCounsellorRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.notify.legalcounsel.LegalCounsellorAddedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.legalcounsel.LegalCounsellorRemovedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static uk.gov.hmcts.reform.fpl.enums.CaseRole.BARRISTER;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;

@Component
@RequiredArgsConstructor
@Slf4j
public class LegalCounselUpdatedEventHandler {

    private final CaseAccessService caseAccessService;
    private final NotificationService notificationService;
    private final CaseUrlService caseUrlService;
    private final EmailNotificationHelper helper;

    @EventListener
    public void handleLegalCounsellorAddedEvent(LegalCounsellorAdded event) {
        CaseData caseData = event.getCaseData();
        Long caseId = caseData.getId();
        String userId = event.getLegalCounsellor().getKey();
        LegalCounsellor legalCounsellor = event.getLegalCounsellor().getValue();

        caseAccessService.grantCaseRoleToUser(caseId, userId, BARRISTER);
        notificationService.sendEmail("2f5826a5-f5c4-41aa-8d75-2bfee7dade87",
            legalCounsellor.getEmail(),
            buildLegalCounsellorAddedNotificationTemplate(caseData),
            caseId);
    }

    @EventListener
    public void handleLegalCounsellorRemovedEvent(LegalCounsellorRemoved event) {
        CaseData caseData = event.getCaseData();
        Long caseId = caseData.getId();
        String userId = event.getLegalCounsellor().getKey();
        LegalCounsellor legalCounsellor = event.getLegalCounsellor().getValue();

        caseAccessService.revokeCaseRoleFromUser(caseId, userId, BARRISTER);
        notificationService.sendEmail("85494117-1030-4c57-a1d7-f6ce32a81454",
            legalCounsellor.getEmail(),
            buildLegalCounsellorRemovedNotificationTemplate(
                caseData, legalCounsellor, event.getSolicitorOrganisationName()
            ),
            caseId);
    }

    public LegalCounsellorAddedNotifyTemplate buildLegalCounsellorAddedNotificationTemplate(CaseData caseData) {
        Long caseId = caseData.getId();

        return LegalCounsellorAddedNotifyTemplate.builder()
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .caseId(formatCCDCaseNumber(caseId))
            .caseUrl(caseUrlService.getCaseUrl(caseId))
            .build();
    }

    private LegalCounsellorRemovedNotifyTemplate buildLegalCounsellorRemovedNotificationTemplate(
        CaseData caseData, LegalCounsellor legalCounsellor, String solicitorOrganisationName) {

        return LegalCounsellorRemovedNotifyTemplate.builder()
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .caseName(caseData.getCaseName())
            .salutation("Dear " + legalCounsellor.getFullName())
            .clientFullName(solicitorOrganisationName)
            .ccdNumber(formatCCDCaseNumber(caseData.getId()))
            .build();
    }

}
