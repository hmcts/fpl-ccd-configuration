package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CourtLevelAllocationService;

import java.util.Map;

@Tag(name = "Allocation decision")
@RestController
@RequestMapping("/callback/allocation-decision")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllocationDecisionController extends CallbackController {
    private final CourtLevelAllocationService service;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        Allocation allocationDecision = service.createDecision(caseData);

        Map<String, Object> data = caseDetails.getData();
        data.put("allocationDecision", allocationDecision);

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        Allocation allocationDecision = service.setAllocationDecisionIfNull(caseData);

        Map<String, Object> data = caseDetails.getData();
        data.put("allocationDecision", allocationDecision);

        return respond(caseDetails);
    }
}
