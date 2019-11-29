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
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;

@Api
@RestController
@RequestMapping("/callback/draft-cmo")
public class DraftCMOController {
    private final ObjectMapper mapper;
    private final DraftCMOService draftCMOService;

    @Autowired
    public DraftCMOController(ObjectMapper mapper,
                              DraftCMOService draftCMOService) {
        this.mapper = mapper;
        this.draftCMOService = draftCMOService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        caseDetails.getData().put("cmoHearingDateList", draftCMOService.getHearingDateDynamicList(caseDetails));

        draftCMOService.prepareCustomDirections(caseDetails);

        setCustomDirectionDropdownLabels(caseDetails);

        caseDetails.getData().put("cmoHearingDateList", draftCMOService.getHearingDateDynamicList(caseDetails));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseManagementOrder caseManagementOrder = draftCMOService.prepareCMO(caseDetails);

        caseDetails.getData().remove("cmoHearingDateList");
        caseDetails.getData().put("caseManagementOrder", caseManagementOrder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private void setCustomDirectionDropdownLabels(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseData.getOthers() != null) {
            caseDetails.getData().put("otherPartiesDropdownLabelCMO",
                draftCMOService.createOtherPartiesAssigneeDropdownKey(caseData.getOthers()));
        }

        caseDetails.getData().put("respondentsDropdownLabelCMO",
            draftCMOService.createRespondentAssigneeDropdownKey(caseData.getRespondents1()));
    }
}
