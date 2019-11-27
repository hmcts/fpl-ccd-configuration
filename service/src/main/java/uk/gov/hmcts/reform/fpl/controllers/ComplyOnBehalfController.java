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
import uk.gov.hmcts.reform.fpl.service.RespondentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;

@Api
@RestController
@RequestMapping("/callback/comply-on-behalf")
public class ComplyOnBehalfController {
    private final ObjectMapper mapper;
    private final DirectionHelperService directionHelperService;
    private final RespondentService respondentService;

    //TODO: integration tests

    @Autowired
    public ComplyOnBehalfController(ObjectMapper mapper,
                                    DirectionHelperService directionHelperService,
                                    RespondentService respondentService) {
        this.mapper = mapper;
        this.directionHelperService = directionHelperService;
        this.respondentService = respondentService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        Map<DirectionAssignee, List<Element<Direction>>> sortedDirections =
            directionHelperService.sortDirectionsByAssignee(caseData.getStandardDirectionOrder().getDirections());

        directionHelperService.addDirectionsToCaseDetails(caseDetails, sortedDirections);

        //TODO: others label
        //TODO: extract to service

        String respondentsLabel =
            respondentService.buildRespondentLabel(defaultIfNull(caseData.getRespondents1(), emptyList()));

        caseDetails.getData().put("respondents1_label", respondentsLabel);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        //TODO: handle COURT user updating a response made by local authority.
        // How can I pick which direction to overwrite??

        // HANDLE SINGLE RESPONSE DIRECTIONS //////////////////////////////////////////////////////////////////////
        // uses directionId and assignee to add and update directions. One response per assignee for a direction.
        Map<DirectionAssignee, List<Element<Direction>>> directionsMap =
            directionHelperService.collectDirectionsToMap(caseData);

        List<DirectionResponse> responses = directionHelperService.getResponses(directionsMap);

        directionHelperService.addResponsesToDirections(
            responses, caseData.getStandardDirectionOrder().getDirections());

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////

        //TODO: handle deleting a response in UI -> compare with caseDetails before?? compare with sdo directions??

        //TODO: extract logic to service
        //TODO: handle directions of others

        // HANDLE MULTI RESPONSE DIRECTIONS /////////////////////////////////////////////////////////////////////////
        // uses the id of the element containing the response to update and add directions.
        Map<DirectionAssignee, List<Element<Direction>>> multiResponseDirections = new HashMap<>();
        multiResponseDirections.put(PARENTS_AND_RESPONDENTS, caseData.getRespondentDirectionsCustom());

        AtomicReference<UUID> id = new AtomicReference<>();

        // Adds assignee and directionId to each response in responses.
        List<Element<DirectionResponse>> respondentResponses =
            multiResponseDirections.get(PARENTS_AND_RESPONDENTS).stream()
                .map(directionElement -> {
                    //need to save directionId to add to response
                    id.set(directionElement.getId());

                    return directionElement.getValue().getResponses();
                })
                .flatMap(List::stream)
                .map(element -> Element.<DirectionResponse>builder()
                    .id(element.getId())
                    .value(element.getValue().toBuilder()
                        .assignee(COURT)
                        .directionId(id.get())
                        .build())
                    .build())
                .collect(toList());

        directionHelperService.addResponseElementsToDirections(
            respondentResponses, caseData.getStandardDirectionOrder().getDirections());

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////

        caseDetails.getData().put("standardDirectionOrder", caseData.getStandardDirectionOrder());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
