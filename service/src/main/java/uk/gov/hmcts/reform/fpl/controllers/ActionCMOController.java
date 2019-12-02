package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.CaseManageOrderActionService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;

@Api
@RestController
@RequestMapping("/callback/action-cmo")
public class ActionCMOController {
    private final ObjectMapper objectMapper;
    private final DraftCMOService draftCMOService;
    private final CaseManageOrderActionService caseManageOrderActionService;

    public ActionCMOController(ObjectMapper objectMapper,
                               DraftCMOService draftCMOService,
                               CaseManageOrderActionService caseManageOrderActionService) {
        this.objectMapper = objectMapper;
        this.draftCMOService = draftCMOService;
        this.caseManageOrderActionService = caseManageOrderActionService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        draftCMOService.prepareCustomDirections(caseDetails.getData());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

        CaseManagementOrder updatedDraftCaseManagementOrder = draftCMOService.prepareCMO(caseDetails.getData());
        CaseData updatedCaseData = caseData.toBuilder()
            .caseManagementOrder(updatedDraftCaseManagementOrder)
            .build();

        CaseManagementOrder caseManagementOrder = caseManageOrderActionService.addDocumentToActionedCaseManagementOrder(
            authorization, userId, caseDetails, updatedCaseData);

        caseDetails.getData().put("caseManagementOrder", caseManagementOrder);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
