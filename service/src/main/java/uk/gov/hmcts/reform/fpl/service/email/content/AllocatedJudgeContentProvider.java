package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.AllocatedJudgeTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllocatedJudgeContentProvider extends AbstractEmailContentProvider {

    private final ObjectMapper mapper;

    public NotifyData buildAllocatedJudgeNotificationParameters(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        AllocatedJudgeTemplate allocatedJudgeTemplate = new AllocatedJudgeTemplate();
        allocatedJudgeTemplate.setJudgeTitle(caseData.getAllocatedJudge().getJudgeTitle().getLabel());
        allocatedJudgeTemplate.setJudgeName(caseData.getAllocatedJudge().getJudgeName());
        allocatedJudgeTemplate.setCaseName(caseData.getCaseName());
        allocatedJudgeTemplate.setCaseUrl(getCaseUrl(caseDetails.getId()));

        return allocatedJudgeTemplate;
    }
}
