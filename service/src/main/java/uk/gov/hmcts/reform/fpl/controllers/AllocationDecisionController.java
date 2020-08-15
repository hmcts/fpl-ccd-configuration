package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CourtLevelAllocationService;
import uk.gov.hmcts.reform.fpl.utils.CaseDataConverter;

@Api
@RestController
@RequestMapping("/callback/allocation-decision")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllocationDecisionController {
    private final CaseDataConverter caseDataConverter;
    private final CourtLevelAllocationService service;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = caseDataConverter.convertToCaseData(callbackRequest);

        Allocation allocationDecision = service.createDecision(caseData);

        CaseData updatedCase = caseData.toBuilder()
            .allocationDecision(allocationDecision)
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataConverter.convertToMap(updatedCase))
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = caseDataConverter.convertToCaseData(callbackRequest);

        Allocation allocationDecision = service.setAllocationDecisionIfNull(caseData);

        CaseData updatedCase = caseData.toBuilder()
            .allocationDecision(allocationDecision)
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataConverter.convertToMap(updatedCase))
            .build();
    }
}
