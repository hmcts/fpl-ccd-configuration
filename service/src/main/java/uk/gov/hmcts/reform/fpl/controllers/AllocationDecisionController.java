package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.AllocationDecision;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CourtAllocationService;

import java.util.Map;

import static java.util.Optional.ofNullable;

@Api
@RestController
@RequestMapping("/callback/allocation-decision")
public class AllocationDecisionController {
    private final ObjectMapper mapper;
    private final CourtAllocationService service;

    @Autowired
    public AllocationDecisionController(ObjectMapper mapper, CourtAllocationService service) {
        this.mapper = mapper;
        this.service = service;
    }

    @PostMapping("/about-to-start")
    //TODO: naming convention for controllers in the past has always been handle this, handle that
    // this should probably change. Controllers handle the callback, the service handles the logic.
    public AboutToStartOrSubmitCallbackResponse checkIfAllocationProposalIsMissing(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        AllocationDecision allocationDecision = service.createDecision(caseData);

        Map<String, Object> data = caseDetails.getData();
        data.put("allocationDecision", allocationDecision);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/about-to-submit")
    // TODO: logic of controller can be extract to service. Could the createDecision method be used??
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        AllocationDecision allocationDecision = caseData.getAllocationDecision();

        AllocationDecision.AllocationDecisionBuilder decisionBuilder = ofNullable(allocationDecision)
            .map(AllocationDecision::toBuilder)
            .orElse(AllocationDecision.builder());

        if (caseData.getAllocationDecision().getProposal() == null) {
            decisionBuilder.proposal(caseData.getAllocationProposal().getProposal());
            decisionBuilder.judgeLevelRadio(null);
        } else {
            decisionBuilder.judgeLevelRadio(null);
        }

        Map<String, Object> data = caseDetails.getData();
        data.put("allocationDecision", decisionBuilder.build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
