package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForSDO;
import uk.gov.hmcts.reform.fpl.service.email.content.base.StandardDirectionOrderContent;

import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionOrderIssuedEmailContentProvider extends StandardDirectionOrderContent {
    private final ObjectMapper mapper;

    public AllocatedJudgeTemplateForSDO buildNotificationParametersForAllocatedJudge(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        Map<String, Object> commonSDOParameters = super.getSDOPersonalisationBuilder(
            caseDetails.getId(), caseData).build();

        AllocatedJudgeTemplateForSDO allocatedJudgeTemplate = new AllocatedJudgeTemplateForSDO();
        allocatedJudgeTemplate.setFamilyManCaseNumber(commonSDOParameters.get("familyManCaseNumber").toString());
        allocatedJudgeTemplate.setLeadRespondentsName(commonSDOParameters.get("leadRespondentsName").toString());
        allocatedJudgeTemplate.setHearingDate(commonSDOParameters.get("hearingDate").toString());
        allocatedJudgeTemplate.setCaseUrl(commonSDOParameters.get("caseUrl").toString());
        allocatedJudgeTemplate.setJudgeTitle(caseData.getStandardDirectionOrder()
            .getJudgeAndLegalAdvisor()
            .getJudgeOrMagistrateTitle());
        allocatedJudgeTemplate.setJudgeName(caseData.getStandardDirectionOrder()
            .getJudgeAndLegalAdvisor()
            .getJudgeName());

        return allocatedJudgeTemplate;
    }
}
