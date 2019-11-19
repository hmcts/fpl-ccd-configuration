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
import uk.gov.hmcts.reform.fpl.service.RespondentService;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;

@Api
@RestController
@RequestMapping("/callback/comply-on-behalf")
public class ComplyOnBehalfController {
    private final ObjectMapper mapper;
    private final DirectionHelperService directionHelperService;
    private final RespondentService respondentService;

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

        Map<String, List<Element<Direction>>> sortedDirections =
            directionHelperService.sortDirectionsByAssignee(caseData.getStandardDirectionOrder().getDirections());

        sortedDirections.forEach((assignee, directions) -> {
            if (!assignee.equals(ALL_PARTIES.getValue())) {
                directions.addAll(sortedDirections.get(ALL_PARTIES.getValue()));
            }

            if (assignee.equals(PARENTS_AND_RESPONDENTS.getValue())) {
                // TODO: currently shows all responses as we are displaying responses field for respondents
                // method will set responses to empty, and then use add all to combine lists. When extracting it will
                // collect many items that have assignee parentsAndRespondentsCustom.

                caseDetails.getData().put("parentsAndRespondentsCustom",
                    directionHelperService.extractPartyResponse(assignee, directions));

            } else {
                caseDetails.getData()
                    .put(assignee.concat("Custom"), directionHelperService.extractPartyResponse(assignee, directions));
            }
        });

        String respondentsLabel = respondentService.buildRespondentLabel(defaultIfNull(caseData.getRespondents1(), emptyList()));

        caseDetails.getData().put("respondents1_label", respondentsLabel);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

//    @PostMapping("about-to-submit")
//    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
//        CaseDetails caseDetails = callbackrequest.getCaseDetails();
//        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
//
//        Map<DirectionAssignee, List<Element<Direction>>> directionsMap =
//            directionHelperService.collectDirectionsToMap(caseData);
//
//        List<DirectionResponse> responses = directionHelperService.getResponses(directionsMap);
//
//        directionHelperService.addResponsesToDirections(
//            responses, caseData.getStandardDirectionOrder().getDirections());
//
//        caseDetails.getData().put("standardDirectionOrder", caseData.getStandardDirectionOrder());
//
//        return AboutToStartOrSubmitCallbackResponse.builder()
//            .data(caseDetails.getData())
//            .build();
//    }
}
