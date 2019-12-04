package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrderAction;
import uk.gov.hmcts.reform.fpl.service.CaseManageOrderActionService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;

import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/action-cmo")
public class ActionCMOController {
    private static final String CASE_MANAGEMENT_ACTION_KEY = "caseManagementOrderAction";

    private final DraftCMOService draftCMOService;
    private final CaseManageOrderActionService caseManageOrderActionService;

    public ActionCMOController(DraftCMOService draftCMOService,
                               CaseManageOrderActionService caseManageOrderActionService) {
        this.draftCMOService = draftCMOService;
        this.caseManageOrderActionService = caseManageOrderActionService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        draftCMOService.prepareCMO(caseData);

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

        CaseManagementOrder caseManagementOrder =
            caseManageOrderActionService.prepareUpdatedDraftCaseManagementOrderForAction(authorization, userId,
                caseDetails);

        caseDetails.getData().put(CASE_MANAGEMENT_ACTION_KEY, ImmutableMap.of("orderDoc",
            caseManagementOrder.getOrderDoc()));

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

        CaseManagementOrderAction caseManagementOrderAction =
            caseManageOrderActionService.getCaseManagementOrderActioned(authorization, userId, caseDetails);

        caseDetails.getData().put(CASE_MANAGEMENT_ACTION_KEY, caseManagementOrderAction);
        caseDetails.getData().remove("caseManagementOrder");
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
