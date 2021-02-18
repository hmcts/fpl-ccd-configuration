package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper.ManagedLATemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.SharedNotifyContentProvider;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ThirdPartyApplicationContentProvider extends SharedNotifyContentProvider {

    public ManagedLATemplate buildManagedLANotification(CaseData caseData) {
        ManagedLATemplate template = super.buildNotifyTemplate(ManagedLATemplate.builder().build(),
            caseData.getId(),
            caseData.getOrders(),
            caseData.getHearing(),
            caseData.getRespondents1());

        template.setThirdParty(getThirdPartyOrgPolicy(caseData).getOrganisation().getOrganisationName());

        return template;
    }

    private OrganisationPolicy getThirdPartyOrgPolicy(CaseData caseData) {
        if (caseData.getOutsourcingPolicy() != null) {
            return caseData.getOutsourcingPolicy();
        } else {
            return caseData.getLocalAuthorityPolicy();
        }
    }
}
