package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllocatedJudgeContentProvider extends AbstractEmailContentProvider {

    private final ObjectMapper mapper;

    public Map<String, Object> buildAllocatedJudgeNotificationParameters(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return ImmutableMap.of(
            "judgeTitle", caseData.getAllocatedJudge().getJudgeTitle().getLabel(),
            "judgeName", caseData.getAllocatedJudge().getJudgeName(),
            "caseName", caseData.getCaseName(),
            "caseUrl", getCaseUrl(caseDetails.getId())
        );
    }
}
