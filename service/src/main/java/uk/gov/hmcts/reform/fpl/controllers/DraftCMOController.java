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
import uk.gov.hmcts.reform.fpl.service.DirectionHelperService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;

import static java.util.Objects.isNull;

@Api
@RestController
@RequestMapping("/callback/draft-cmo")
public class DraftCMOController {
    private final ObjectMapper mapper;
    private final DraftCMOService draftCMOService;
    private final DirectionHelperService directionHelperService;

    @Autowired
    public DraftCMOController(ObjectMapper mapper,
                              DraftCMOService draftCMOService,
                              DirectionHelperService directionHelperService) {
        this.mapper = mapper;
        this.draftCMOService = draftCMOService;
        this.directionHelperService = directionHelperService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (!isNull(caseData.getCaseManagementOrder())) {
            directionHelperService.sortDirectionsByAssignee(caseData.getCaseManagementOrder().getDirections())
                .forEach((key, value) -> caseDetails.getData().put(key, value));
        } else {
            draftCMOService.removeExistingCustomDirections(caseDetails);
        }

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
}
