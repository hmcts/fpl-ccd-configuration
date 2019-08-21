package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
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

import java.util.List;

import static java.util.stream.Collectors.toList;

@Api
@RestController
@RequestMapping("/callback/comply-with-directions")
public class ComplyWithOrdersController {

    private final ObjectMapper mapper;

    public ComplyWithOrdersController(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        // create list of directions where ids that match are updated.
        List<Element<Direction>> updatedCMO = caseData.getCmoCollection().get(0).getValue().getDirections()
            .stream()
            .map((direction) -> caseData.getCafcassDirections()
                .stream()
                .filter(x -> x.getId().equals(direction.getId()))
                .findFirst()
                .orElse(direction))
            .collect(toList());

        // remove old list
        caseData.getCmoCollection().get(0).getValue().getDirections().clear();
        // add new list
        caseData.getCmoCollection().get(0).getValue().getDirections().addAll(updatedCMO);
        // place in case data
        caseDetails.getData().put("cmoCollection", caseData.getCmoCollection());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
