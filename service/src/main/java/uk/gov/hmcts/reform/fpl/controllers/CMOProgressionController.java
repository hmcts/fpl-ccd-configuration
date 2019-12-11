package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ActionCmoService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;

@Api
@RestController
@RequestMapping("/callback/cmo-progression")
public class CMOProgressionController {
    private final DraftCMOService draftCMOService;
    private final ActionCmoService actionCmoService;
    private final ObjectMapper mapper;

    public CMOProgressionController(DraftCMOService draftCMOService,
                                    ActionCmoService actionCmoService,
                                    ObjectMapper mapper) {
        this.draftCMOService = draftCMOService;
        this.actionCmoService = actionCmoService;
        this.mapper = mapper;
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        // This event is fired at the end of the draftCMO event and the actionCMO event
        // If it is the draftCMO event then caseManagementOrder should not be null otherwise it will be
        if (caseData.getCaseManagementOrder() != null) {
            draftCMOService.progressDraftCMO(caseDetails.getData(), caseData.getCaseManagementOrder());
        } else {
            actionCmoService.progressCMOToAction(caseDetails, caseData.getCmoToAction());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
