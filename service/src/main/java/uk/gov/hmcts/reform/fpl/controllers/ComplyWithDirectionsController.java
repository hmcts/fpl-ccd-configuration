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

        List<Element<Direction>> allPartyDirections = directionHelperService
            .getDirectionsForAssignee(caseData.getStandardDirectionOrder().getDirections(), ALL_PARTIES);

        Map<String, List<Element<Direction>>> sortedDirections =
            directionHelperService.sortDirectionsByAssignee(caseData.getStandardDirectionOrder().getDirections());

        sortedDirections.forEach((assignee, directions) -> directionHelperService
            .addAssigneeDirectionKeyValuePairsToCaseData(assignee, allPartyDirections, caseDetails));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/on-behalf/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart2(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<Direction>> respondentsDirections = directionHelperService
            .getDirectionsForAssignee(caseData.getStandardDirectionOrder().getDirections(), PARENTS_AND_RESPONDENTS);

        caseDetails.getData().put("parentsAndRespondentsCustom", respondentsDirections);

        List<Element<Respondent>> respondents = defaultIfNull(caseData.getRespondents1(), emptyList());

        StringBuilder sb = new StringBuilder();

        if (isNotEmpty(respondents)) {
            AtomicInteger i = new AtomicInteger(1);

            respondents.forEach(y -> {
                    sb.append("Respondent")
                        .append(" ")
                        .append(i)
                        .append(" ")
                        .append("-")
                        .append(" ")
                        .append(y.getValue().getParty().firstName)
                        .append(" ")
                        .append(y.getValue().getParty().lastName)
                        .append("\n");

                    i.incrementAndGet();
                });

        } else {
            sb.append("No respondents on the case");
        }

        caseDetails.getData().put("respondents1_label", sb.toString());

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
