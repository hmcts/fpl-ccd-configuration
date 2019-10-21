package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.SDOSubmittedEvent;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CourtLevelAllocationService;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/allocation-decision")
public class AllocationDecisionController {
    private final ObjectMapper mapper;
    private final CourtLevelAllocationService service;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MapperService mapperService;

    @Autowired
    public AllocationDecisionController(ObjectMapper mapper, CourtLevelAllocationService service, ApplicationEventPublisher applicationEventPublisher, MapperService mapperService) {
        this.mapper = mapper;
        this.service = service;
        this.applicationEventPublisher = applicationEventPublisher;
        this.mapperService = mapperService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        Allocation allocationDecision = service.createDecision(caseData);

        Map<String, Object> data = caseDetails.getData();
        data.put("allocationDecision", allocationDecision);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest,
                                                                    @RequestHeader(value = "user-id") String userId, @RequestHeader(value = "authorization") String authorization) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        Allocation allocationDecision = service.setAllocationDecisionIfNull(caseData);

        Map<String, Object> data = caseDetails.getData();
        data.put("allocationDecision", allocationDecision);

        applicationEventPublisher.publishEvent(new SDOSubmittedEvent(callbackRequest, authorization, userId));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
