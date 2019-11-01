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
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DirectionHelperService;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;

@Api
@RestController
@RequestMapping("/callback/comply-with-directions")
public class ComplyWithDirectionsController {
    private final ObjectMapper mapper;
    private final DirectionHelperService directionHelperService;

    @Autowired
    public ComplyWithDirectionsController(ObjectMapper mapper, DirectionHelperService directionHelperService) {
        this.mapper = mapper;
        this.directionHelperService = directionHelperService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        // need to fetch existing responses and place in UI placeholders

        List<Element<Direction>> allPartyDirections = directionHelperService
            .getDirectionsForAssignee(caseData.getStandardDirectionOrder().getDirections(), ALL_PARTIES);

        directionHelperService.sortDirectionsByAssignee(caseData.getStandardDirectionOrder().getDirections())
            .forEach((key, value) -> caseDetails.getData().put(key, allPartyDirections));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        //get direction responses and add them to collection of responses.

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
