package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.StandardDirectionOrderContent;

import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassEmailContentProviderSDOIssued extends StandardDirectionOrderContent {
    private final CafcassLookupConfiguration config;

    public SDONotifyData getNotifyData(CaseData caseData) {
        return SDONotifyData.builder()
            .title(config.getCafcass(caseData.getCaseLocalAuthority()).getName())
            .documentLink(linkToAttachedDocument(caseData.getStandardDirectionOrder().getOrderDoc()))
            .familyManCaseNumber(getFamilyManCaseNumber(caseData))
            .leadRespondentsName(getLeadRespondentsName(caseData))
            .hearingDate(getHearingDate(caseData))
            .reference(caseData.getId().toString())
            .caseUrl(getCaseUrl(caseData.getId(), "OrdersTab"))
            .callout(buildCallout(caseData))
            .build();
    }
}
