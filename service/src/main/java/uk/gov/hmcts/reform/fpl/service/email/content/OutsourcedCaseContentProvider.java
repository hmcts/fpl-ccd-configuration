package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.OutsourcedCaseTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.SharedNotifyContentProvider;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OutsourcedCaseContentProvider extends SharedNotifyContentProvider {

    public OutsourcedCaseTemplate buildNotifyLAOnOutsourcedCaseTemplate(CaseData caseData) {
        OutsourcedCaseTemplate template = super.buildNotifyTemplate(
            OutsourcedCaseTemplate.builder().build(),
            caseData.getId(),
            caseData.getOrders(),
            caseData.getHearing(),
            caseData.getRespondents1());

        template.setThirdParty(caseData.getOutsourcingPolicy().getOrganisation().getOrganisationName());

        return template;
    }
}
