package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/allocation-decision")
public class AllocationDecisionController {

    @PostMapping("/about-to-start")
    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse checkIfAllocationProposalIsMissing(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        Map<String, Object> data = caseDetails.getData();

        Map<String, Object> allocationDecision = (Map<String, Object>) data
            .computeIfAbsent("allocationDecision", (key) -> new HashMap<>());
        allocationDecision.put("allocationProposalPresent", checkProposal(data));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private String checkProposal(Map<String, Object> data) {
        return data.containsKey("allocationProposal") ? "Yes" : "No";
    }
}
