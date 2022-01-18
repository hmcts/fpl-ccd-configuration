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
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.PlacementService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.UUID;

@Api
@RestController
@RequestMapping("/callback/placementNotice")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlacementNoticeController extends CallbackController {

    private final PlacementService placementService;
    private final RespondentService respondentService;

    private final ObjectMapper mapper;


    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);
        final CaseDetailsMap caseProperties = CaseDetailsMap.caseDetailsMap(caseDetails);


        caseProperties.putIfNotEmpty("hasExistingPlacements",
            YesNo.from(!caseData.getPlacementEventData().getPlacements().isEmpty())
        );
        caseProperties.putIfNotEmpty("placementList", asDynamicList(caseData.getPlacementEventData().getPlacements()));

        return respond(caseProperties);
    }

    @PostMapping("placement-application/mid-event")
    public AboutToStartOrSubmitCallbackResponse handlePlacementSelection(@RequestBody CallbackRequest request) {
        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseDetailsMap caseProperties = CaseDetailsMap.caseDetailsMap(caseDetails);

        return respond(caseProperties);

    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseDetailsMap caseProperties = CaseDetailsMap.caseDetailsMap(caseDetails);

        return respond(caseProperties);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest request) {
        final CaseData caseData = getCaseData(request);
        final CaseData caseDataBefore = getCaseDataBefore(request);

        publishEvents(placementService.getEvents(caseData, caseDataBefore));
    }

    public DynamicList asDynamicList(List<Element<Placement>> placements) {
        return asDynamicList(placements, null);
    }

    public DynamicList asDynamicList(List<Element<Placement>> placements, UUID selectedId) {
        return ElementUtils.asDynamicList(placements, selectedId, Placement::toLabel);
    }


}
