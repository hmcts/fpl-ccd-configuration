package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CMO;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;
import static org.springframework.util.ObjectUtils.isEmpty;

@Api
@RestController
@RequestMapping("/callback/draft-SDO")
public class DraftController {

    private final ObjectMapper mapper;

    public DraftController(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        //pre populate standard directions
        List<Element<Direction>> directions = ImmutableList.of(
            Element.<Direction>builder()
                .id(UUID.randomUUID())
                .value(Direction.builder()
                    .title("Arrange an advocates' meeting")
                    .assignee("cafcassDirections")
                    .build())
                .build());

        caseDetails.getData().put("cmo", CMO.builder().directions(directions).build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        // sort directions by role
        Map<String, List<Element<Direction>>> directionsByRole = caseData.getCmo().getDirections().stream()
            .collect(groupingBy(direction -> direction.getValue().getAssignee()));

        // add directions into their respective pre defined collections
        caseDetails.getData().putAll(directionsByRole);

        // populate CMOs collection of collections
        if (isEmpty(caseData.getCmoCollection())) {
            // when empty, add the first thing
            caseDetails.getData().put("cmoCollection", ImmutableList.of(Element.builder()
                .value(caseData.getCmo())
                .build()));
        } else {
            // when second CMO, add to list
            caseData.getCmoCollection().add(0, Element.<CMO>builder()
                .id(UUID.randomUUID())
                .value(caseData.getCmo())
                .build());

            caseDetails.getData().put("cmoCollection", caseData.getCmoCollection());
        }

        // remove old CMO
        caseDetails.getData().remove("cmo");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
