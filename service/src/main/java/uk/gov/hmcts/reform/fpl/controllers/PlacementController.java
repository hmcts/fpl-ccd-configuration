package uk.gov.hmcts.reform.fpl.controllers;

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
import uk.gov.hmcts.reform.fpl.enums.Cardinality;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.PlacementService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.Cardinality.ZERO;
import static uk.gov.hmcts.reform.fpl.model.event.PlacementEventData.NOTICE_GROUP;
import static uk.gov.hmcts.reform.fpl.model.event.PlacementEventData.PLACEMENT_GROUP;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.addFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@Api
@RestController
@RequestMapping("/callback/placement")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlacementController extends CallbackController {

    private final PlacementService placementService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);
        final CaseDetailsMap caseProperties = CaseDetailsMap.caseDetailsMap(caseDetails);

        final PlacementEventData eventData = placementService.prepareChildren(caseData);

        final Cardinality childrenCardinality = eventData.getPlacementChildrenCardinality();

        if (childrenCardinality == ZERO) {
            return respond(caseProperties, List.of("There are no children available for placement application"));
        }

        caseProperties.put("placementChildrenCardinality", childrenCardinality);
        caseProperties.putIfNotEmpty("placementChildrenList", eventData.getPlacementChildrenList());

        addFields(caseProperties, eventData, PLACEMENT_GROUP, NOTICE_GROUP);

        return respond(caseProperties);
    }

    @PostMapping("child-selection/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleChildSelection(@RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseDetailsMap caseProperties = CaseDetailsMap.caseDetailsMap(caseDetails);
        final CaseData caseData = getCaseData(caseDetails);

        final PlacementEventData eventData = placementService.preparePlacement(caseData);

        addFields(caseProperties, eventData, PLACEMENT_GROUP, NOTICE_GROUP);

        return respond(caseProperties);
    }

    @PostMapping("documents-upload/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleDocumentsUploaded(@RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseDetailsMap caseProperties = CaseDetailsMap.caseDetailsMap(caseDetails);
        final CaseData caseData = getCaseData(caseDetails);

        final List<String> errors = placementService.checkDocuments(caseData);

        return respond(caseProperties, errors);
    }

    @PostMapping("notices-upload/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleNoticesUploaded(@RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseDetailsMap caseProperties = CaseDetailsMap.caseDetailsMap(caseDetails);
        final CaseData caseData = getCaseData(caseDetails);

        final List<String> errors = placementService.checkNotices(caseData);

        if (!errors.isEmpty()) {
            return respond(caseProperties, errors);
        }

        final PlacementEventData eventData = placementService.preparePayment(caseData);

        caseProperties.putIfNotEmpty("placement", eventData.getPlacement());
        caseProperties.putIfNotEmpty("placementPaymentRequired", eventData.getPlacementPaymentRequired());
        caseProperties.putIfNotEmpty("placementFee", eventData.getPlacementFee());

        return respond(caseProperties);
    }

    @PostMapping("payment-details/mid-event")
    public AboutToStartOrSubmitCallbackResponse handlePaymentsDetails(@RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseDetailsMap caseProperties = CaseDetailsMap.caseDetailsMap(caseDetails);
        final CaseData caseData = getCaseData(caseDetails);

        final List<String> errors = placementService.checkPayment(caseData);

        caseProperties.putIfNotEmpty("placementPayment", caseData.getPlacementEventData().getPlacementPayment());

        return respond(caseProperties, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        final PlacementEventData placementEventData = placementService.savePlacement(caseData);

        caseDetails.getData().put("placements", placementEventData.getPlacements());
        caseDetails.getData().put("placementsNonConfidential", placementEventData.getPlacementsNonConfidential());

        removeTemporaryFields(caseDetails, PlacementEventData.class);

        return respond(caseDetails.getData());
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest request) {

        final CaseData caseData = getCaseData(request);
        final CaseData caseDataBefore = getCaseDataBefore(request);

        publishEvents(placementService.getEvents(caseData, caseDataBefore));
    }

}
