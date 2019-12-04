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
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrderAction;
import uk.gov.hmcts.reform.fpl.service.CaseManageOrderActionService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;

import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/action-cmo")
public class ActionCMOController {
    private final CaseManageOrderActionService caseManageOrderActionService;
    private final DraftCMOService draftCMOService;
    private final ObjectMapper mapper;

    public ActionCMOController(CaseManageOrderActionService caseManageOrderActionService,
                               DraftCMOService draftCMOService,
                               ObjectMapper mapper) {
        this.caseManageOrderActionService = caseManageOrderActionService;
        this.draftCMOService = draftCMOService;
        this.mapper = mapper;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        final CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.put("cmoHearingDateList",
            draftCMOService.buildDynamicListFromHearingDetails(caseData.getHearingDetails()));

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

        caseManageOrderActionService.prepareUpdatedDraftCMOForAction(authorization, userId, caseDetails);

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

        caseDetails.getData().put("caseManagementOrderAction", caseManagementOrderAction);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
