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
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DirectionHelperService;

import java.util.List;
import java.util.Map;

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
        Map<DirectionAssignee, List<Element<Direction>>> sortedDirections;

        if (callbackrequest.getEventId().equals("COMPLY_LOCAL_AUTHORITY")) {
            sortedDirections =
                directionHelperService.sortDirectionsByAssignee(caseData.getStandardDirectionOrder().getDirections());
        } else {
            sortedDirections =
                directionHelperService.sortDirectionsByAssignee(caseData.getCaseManagementOrder().getDirections());
        }

        sortedDirections.forEach((assignee, directions) -> {
            if (!assignee.equals(ALL_PARTIES)) {
                directions.addAll(sortedDirections.get(ALL_PARTIES));
                directionHelperService.addAssigneeDirectionKeyValuePairsToCaseData(assignee, directions, caseDetails);
            }
        });

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        Map<DirectionAssignee, List<Element<Direction>>> directionsMap =
            directionHelperService.collectDirectionsToMap(caseData);

        List<DirectionResponse> responses = directionHelperService.getResponses(directionsMap);

        if (callbackrequest.getEventId().equals("COMPLY_LOCAL_AUTHORITY")) {
            directionHelperService.addResponsesToDirections(
                responses, caseData.getStandardDirectionOrder().getDirections());

            caseDetails.getData().put("standardDirectionOrder", caseData.getStandardDirectionOrder());
        } else {
            directionHelperService.addResponsesToDirections(
                responses, caseData.getCaseManagementOrder().getDirections());

            caseDetails.getData().put("caseManagementOrder", caseData.getCaseManagementOrder());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
