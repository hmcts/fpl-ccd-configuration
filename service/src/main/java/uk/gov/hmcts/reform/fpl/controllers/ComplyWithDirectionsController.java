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
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DirectionHelperService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;

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

        Map<DirectionAssignee, List<Element<Direction>>> sortedDirections =
            directionHelperService.sortDirectionsByAssignee(caseData.getStandardDirectionOrder().getDirections());

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

        directionHelperService.addResponsesToDirections(
            responses, caseData.getStandardDirectionOrder().getDirections());

        caseDetails.getData().put("standardDirectionOrder", caseData.getStandardDirectionOrder());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
