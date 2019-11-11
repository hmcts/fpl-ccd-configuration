package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;

import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/draft-cmo")
public class DraftCMOController {
    private final ObjectMapper mapper;
    private final DraftCMOService draftCMOService;

    @Autowired
    public DraftCMOController(ObjectMapper mapper, DraftCMOService draftCMOService) {
        this.mapper = mapper;
        this.draftCMOService = draftCMOService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        caseData.put("cmoHearingDateList", draftCMOService.getHearingDatesDynamic(caseDetails));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        DynamicList list = mapper.convertValue(caseDetails.getData().get("cmoHearingDateList"), DynamicList.class);

        CaseData updatedCaseData = caseData.toBuilder()
            .caseManagementOrder(CaseManagementOrder.builder().build())
            .build();

        CaseManagementOrder caseManagementOrder = updatedCaseData.getCaseManagementOrder()
            .toBuilder().hearingDate(list.getValue().getLabel())
            .hearingDateId(list.getValue().getCode())
            .build();

        caseDetails.getData().remove("cmoHearingDateList");
        caseDetails.getData().put("caseManagementOrder", caseManagementOrder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
