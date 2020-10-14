package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllocatedJudgeContentProvider extends AbstractEmailContentProvider {

    public AllocatedJudgeTemplate buildNotificationParameters(CaseData caseData) {

        AllocatedJudgeTemplate allocatedJudgeTemplate = new AllocatedJudgeTemplate();
        allocatedJudgeTemplate.setJudgeTitle(caseData.getAllocatedJudge().getJudgeOrMagistrateTitle());
        allocatedJudgeTemplate.setJudgeName(caseData.getAllocatedJudge().getJudgeName());
        allocatedJudgeTemplate.setCaseName(caseData.getCaseName());
        allocatedJudgeTemplate.setCaseUrl(getCaseUrl(caseData.getId()));
        allocatedJudgeTemplate.setFamilyManCaseNumber(caseData.getFamilyManCaseNumber());

        return allocatedJudgeTemplate;
    }
}
