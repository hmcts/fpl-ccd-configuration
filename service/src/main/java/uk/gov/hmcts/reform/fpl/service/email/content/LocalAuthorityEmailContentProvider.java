package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.StandardDirectionOrderContent;

import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalAuthorityEmailContentProvider extends StandardDirectionOrderContent {
    private final LocalAuthorityNameLookupConfiguration config;

    public SDONotifyData buildStandardDirectionOrderIssuedNotification(CaseData caseData) {
        return SDONotifyData.builder()
            .title(config.getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .familyManCaseNumber(getFamilyManCaseNumber(caseData))
            .leadRespondentsName(getLeadRespondentsName(caseData))
            .hearingDate(getHearingDate(caseData))
            .reference(caseData.getId().toString())
            .caseUrl(getCaseUrl(caseData.getId(), "OrdersTab"))
            .callout(buildCallout(caseData))
            .build();
    }

    public BaseCaseNotifyData buildNoticeOfPlacementOrderUploadedNotification(CaseData caseData) {
        return BaseCaseNotifyData.builder()
            .respondentLastName(getFirstRespondentLastName(caseData))
            .caseUrl(getCaseUrl(caseData.getId(), "PlacementTab"))
            .build();
    }
}
