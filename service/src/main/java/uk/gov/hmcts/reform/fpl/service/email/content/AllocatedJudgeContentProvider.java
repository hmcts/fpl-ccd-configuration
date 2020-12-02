package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllocatedJudgeContentProvider extends AbstractEmailContentProvider {

    public AllocatedJudgeTemplate buildNotificationParameters(CaseData caseData) {

        return AllocatedJudgeTemplate.builder()
            .judgeTitle(caseData.getAllocatedJudge().getJudgeOrMagistrateTitle())
            .judgeName(caseData.getAllocatedJudge().getJudgeName())
            .caseName(caseData.getCaseName())
            .caseUrl(getCaseUrl(caseData.getId()))
            .familyManCaseNumber(defaultIfNull(caseData.getFamilyManCaseNumber(), ""))
            .build();
    }
}
