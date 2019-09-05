package uk.gov.hmcts.reform.fpl.controllers;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/allocation-decision")
public class AllocationDecisionController {

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse checkIfAllocationProposalIsMissing(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        Map<String, Object> data = caseDetails.getData();

        data.put("missingProposal", checkProposal(data));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private String checkProposal(Map<String, Object> data) {
        return data.containsKey("allocationProposal") ? "" : "Yes";
    }
}
