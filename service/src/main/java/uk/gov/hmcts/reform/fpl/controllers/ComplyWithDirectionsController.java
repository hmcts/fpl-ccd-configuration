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
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;

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

        directionHelperService.sortDirectionsByAssignee(caseData.getStandardDirectionOrder().getDirections())
            .forEach((key, value) ->
                caseDetails.getData().put(key, directionHelperService.extractPartyResponse(key, allPartyDirections)));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        // TODO: only local authority directions
        List<Element<Direction>> localAuthorityDirections = addHiddenVariablesToResponse(caseData);

        directionHelperService.addResponsesToDirections(
            localAuthorityDirections, caseData.getStandardDirectionOrder().getDirections());

        caseDetails.getData().put("standardDirectionOrder", caseData.getStandardDirectionOrder());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    // TODO: extract to service... how can this work dynamically for different roles
    private List<Element<Direction>> addHiddenVariablesToResponse(CaseData caseData) {
        return caseData.getLocalAuthorityDirections().stream()
            .filter(element -> isNotEmpty(element.getValue().getResponse().getComplied()))
            .map(x -> Element.<Direction>builder()
                .id(x.getId())
                .value(x.getValue().toBuilder()
                    .response(x.getValue().getResponse().toBuilder()
                        .assignee(LOCAL_AUTHORITY)
                        .directionId(x.getId())
                        .build())
                    .build())
                .build())
            .collect(Collectors.toList());
    }
}
