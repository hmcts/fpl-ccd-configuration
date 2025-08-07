package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.notify.legalcounsel.LegalCounsellorAddedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.legalcounsel.LegalCounsellorRemovedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;

@Component
public class LegalCounsellorEmailContentProvider extends AbstractEmailContentProvider {

    @Autowired
    private EmailNotificationHelper helper;

    public LegalCounsellorAddedNotifyTemplate buildLegalCounsellorAddedNotificationTemplate(CaseData caseData) {
        Long caseId = caseData.getId();

        return LegalCounsellorAddedNotifyTemplate.builder()
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .caseId(formatCCDCaseNumber(caseId))
            .caseUrl(getCaseUrl(caseId))
            .build();
    }

    public LegalCounsellorRemovedNotifyTemplate buildLegalCounsellorRemovedNotificationTemplate(
        CaseData caseData, LegalCounsellorRemoved event) {

        LegalCounsellor legalCounsellor = event.getLegalCounsellor();
        String solicitorOrganisationName = event.getSolicitorOrganisationName();

        return LegalCounsellorRemovedNotifyTemplate.builder()
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .caseName(caseData.getCaseName())
            .salutation("Dear " + legalCounsellor.getFullName())
            .clientFullName(solicitorOrganisationName)
            .ccdNumber(formatCCDCaseNumber(caseData.getId()))
            .build();
    }
}
