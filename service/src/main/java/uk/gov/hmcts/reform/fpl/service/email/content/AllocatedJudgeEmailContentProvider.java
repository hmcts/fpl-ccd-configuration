package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.StandardDirectionOrderContent;

import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllocatedJudgeEmailContentProvider extends StandardDirectionOrderContent {
    private final ObjectMapper mapper;

    public Map<String, Object> buildStandardDirectionOrderIssuedNotification(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return super.getSDOPersonalisationBuilder(caseDetails.getId(), caseData)
            .put("judgeTitle", caseData.getStandardDirectionOrder().getJudgeAndLegalAdvisor().getJudgeOrMagistrateTitle())
            .put("judgeName", caseData.getStandardDirectionOrder().getJudgeAndLegalAdvisor().getJudgeName())
            .build();
    }
}
