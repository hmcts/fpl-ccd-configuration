package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
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
import uk.gov.hmcts.reform.fpl.model.AllocationProposal;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Map;

import static java.util.Optional.ofNullable;

@Api
@RestController
@RequestMapping("/callback/allocation-decision")
public class AllocationDecisionController {

    private final ObjectMapper mapper;

    @Autowired
    public AllocationDecisionController(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse checkIfAllocationProposalIsMissing(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);


        AllocationDecision allocationDecision = caseData.getAllocationDecision();

        AllocationDecision.AllocationDecisionBuilder decisionBuilder = ofNullable(allocationDecision)
            .map(AllocationDecision::toBuilder)
            .orElse(AllocationDecision.builder());

        decisionBuilder.allocationProposalPresent(checkIfAllocationProposalIsPresent(caseData.getAllocationProposal()));

        Map<String, Object> data = caseDetails.getData();
        data.put("allocationDecision", decisionBuilder.build());
        
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private String checkIfAllocationProposalIsPresent(AllocationProposal data) {
        return data != null && StringUtils.isNotEmpty(data.getProposal()) ? "Yes" : "No";
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        AllocationDecision allocationDecision = caseData.getAllocationDecision();

        AllocationDecision.AllocationDecisionBuilder decisionBuilder = ofNullable(allocationDecision)
            .map(AllocationDecision::toBuilder)
            .orElse(AllocationDecision.builder());

        if (caseData.getAllocationDecision().getProposal() == null) {
            decisionBuilder.proposal(caseData.getAllocationProposal().getProposal());
            Map<String, Object> data = caseDetails.getData();
            data.put("allocationDecision", decisionBuilder.build());
        } else {
            decisionBuilder.proposal(caseData.getAllocationDecision().getProposal());
            decisionBuilder.proposalReason(caseData.getAllocationDecision().getProposalReason());
            Map<String, Object> data = caseDetails.getData();
            data.put("allocationDecision", decisionBuilder.build());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
