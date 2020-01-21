package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
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
import uk.gov.hmcts.reform.fpl.service.CommonDirectionService;
import uk.gov.hmcts.reform.fpl.service.PrepareDirectionsForDataStoreService;
import uk.gov.hmcts.reform.fpl.service.PrepareDirectionsForUsersService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;

@Api
@RestController
@RequestMapping("/callback/comply-with-directions")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ComplyWithDirectionsController {
    private final ObjectMapper mapper;
    private final CommonDirectionService commonDirectionService;
    private final PrepareDirectionsForUsersService prepareDirectionsForUsersService;
    private final PrepareDirectionsForDataStoreService prepareDirectionsForDataStoreService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        //TODO: could exist in sdo vs cmo service.
        List<Element<Direction>> directionsToComplyWith = commonDirectionService.getDirectionsToComplyWith(caseData);

        Map<DirectionAssignee, List<Element<Direction>>> sortedDirections =
            commonDirectionService.sortDirectionsByAssignee(directionsToComplyWith);

        commonDirectionService.addEmptyDirectionsForAssigneeNotInMap(sortedDirections);

        sortedDirections.forEach((assignee, directions) -> {
            if (!assignee.equals(ALL_PARTIES)) {
                directions.addAll(sortedDirections.get(ALL_PARTIES));
                prepareDirectionsForUsersService.addAssigneeDirectionKeyValuePairsToCaseData(
                    assignee, directions, caseDetails);
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
            commonDirectionService.collectDirectionsToMap(caseData);

        directionsMap.forEach(prepareDirectionsForDataStoreService::addHiddenValuesToResponseForAssignee);

        List<DirectionResponse> responses = commonDirectionService.getResponses(directionsMap);
        List<Element<Direction>> directionsToComplyWith = commonDirectionService.getDirectionsToComplyWith(caseData);

        prepareDirectionsForDataStoreService.addResponsesToDirections(responses, directionsToComplyWith);

        //TODO: new service for sdo vs cmo in placing directions
        if (caseData.getServedCaseManagementOrders().isEmpty()) {
            caseDetails.getData().put("standardDirectionOrder", caseData.getStandardDirectionOrder());
        } else {
            caseDetails.getData().put("servedCaseManagementOrders", caseData.getServedCaseManagementOrders());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
