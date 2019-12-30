package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.CaseManagementOrderProgressionService;

@Api
@RestController
@RequestMapping("/callback/cmo-progression")
public class CaseManagementOrderProgressionController {
    private final CaseManagementOrderProgressionService progressionService;

    @Autowired
    public CaseManagementOrderProgressionController(CaseManagementOrderProgressionService progressionService) {
        this.progressionService = progressionService;
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        String eventId = (String) caseDetails.getData().remove("cmoEventId");

        progressionService.handleCaseManagementOrderProgression(caseDetails, eventId);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
