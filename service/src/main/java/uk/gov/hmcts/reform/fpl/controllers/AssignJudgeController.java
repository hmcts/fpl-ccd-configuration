package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Api
@RestController
@RequestMapping("/callback/assign-judge")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AssignJudgeController {
    private final ObjectMapper mapper;

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (isNotEmpty(caseData.getAllocatedJudge())) {
            String assignedJudgeLabel = buildAssignedJudgeLabel(caseData.getAllocatedJudge());

            JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
                .allocatedJudgeLabel(assignedJudgeLabel)
                .build();

            caseDetails.getData().put("judgeAndLegalAdvisor", judgeAndLegalAdvisor);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private String buildAssignedJudgeLabel(Judge judge) {
        return String.format("Case assigned to: %s %s", judge.getJudgeOrMagistrateTitle(), judge.getJudgeName());
    }
}
