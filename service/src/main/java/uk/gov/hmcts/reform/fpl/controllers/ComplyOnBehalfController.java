package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ComplyOnBehalfEvent;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DirectionHelperService;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Api
@RestController
@RequestMapping("/callback/comply-on-behalf")
public class ComplyOnBehalfController {
    private final ObjectMapper mapper;
    private final DirectionHelperService directionHelperService;
    private final RespondentService respondentService;
    private final OthersService othersService;

    @Autowired
    public ComplyOnBehalfController(ObjectMapper mapper,
                                    DirectionHelperService directionHelperService,
                                    RespondentService respondentService,
                                    OthersService othersService) {
        this.mapper = mapper;
        this.directionHelperService = directionHelperService;
        this.respondentService = respondentService;
        this.othersService = othersService;
    }

    //TODO: filter responses with different userName in aboutToStart. Code below makes the assumption that only
    // the same responder will be able edit a response. Currently any solicitor can amend a response but the
    // name of the responder does not change.
    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<Direction>> directionsToComplyWith = directionHelperService.getDirectionsToComplyWith(caseData);

        Map<DirectionAssignee, List<Element<Direction>>> sortedDirections =
            directionHelperService.sortDirectionsByAssignee(directionsToComplyWith);

        directionHelperService.addEmptyDirectionsForAssigneeNotInMap(sortedDirections);

        directionHelperService.addDirectionsToCaseDetails(
            caseDetails, sortedDirections, ComplyOnBehalfEvent.valueOf(callbackrequest.getEventId()));

        caseDetails.getData().put("respondents_label", getRespondentsLabel(caseData));
        caseDetails.getData().put("others_label", getOthersLabel(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackrequest,
        @RequestHeader(value = "authorization") String authorisation) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        directionHelperService.addComplyOnBehalfResponsesToDirectionsInOrder(
            caseData, ComplyOnBehalfEvent.valueOf(callbackrequest.getEventId()), authorisation);

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

    private String getRespondentsLabel(CaseData caseData) {
        return respondentService.buildRespondentLabel(defaultIfNull(caseData.getRespondents1(), emptyList()));
    }

    private String getOthersLabel(CaseData caseData) {
        return othersService.buildOthersLabel(defaultIfNull(caseData.getOthers(), Others.builder().build()));
    }
}
