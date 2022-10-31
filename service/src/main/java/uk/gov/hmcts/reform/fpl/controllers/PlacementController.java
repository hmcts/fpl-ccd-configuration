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
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.ZERO;
import static uk.gov.hmcts.reform.fpl.model.event.PlacementEventData.PLACEMENT_GROUP;
import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.putFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@Api
@RestController
@RequestMapping("/callback/placement")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlacementController extends CallbackController {

    private final PlacementService placementService;
    private final RespondentService respondentService;
    private final CoreCaseDataService coreCaseDataService;

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

        putFields(caseProperties, eventData, PLACEMENT_GROUP);

        if (isNotEmpty(caseData.getAllRespondents())) {
            caseProperties.put("hasRespondents", "Yes");
            caseProperties.put("placementRespondentsLabel",
                respondentService.buildRespondentLabel(caseData.getAllRespondents()));
            caseProperties.put("respondentsSelector", newSelector(caseData.getAllRespondents().size()));
        }

        return respond(caseProperties);
    }

    @PostMapping("child-selection/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleChildSelection(@RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseDetailsMap caseProperties = CaseDetailsMap.caseDetailsMap(caseDetails);
        final CaseData caseData = getCaseData(caseDetails);

        final PlacementEventData eventData = placementService.preparePlacement(caseData);

        putFields(caseProperties, eventData, PLACEMENT_GROUP);

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

    @PostMapping("notices-respondents/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleSelectingParties(@RequestBody CallbackRequest request) {
        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseDetailsMap caseProperties = CaseDetailsMap.caseDetailsMap(caseDetails);
        final CaseData caseData = getCaseData(caseDetails);

        PlacementEventData eventData = placementService.preparePayment(caseData);
        caseProperties.put("placement", eventData.getPlacement());
        caseProperties.put("placementPaymentRequired", eventData.getPlacementPaymentRequired());
        caseProperties.put("placementFee", eventData.getPlacementFee());

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

        final PlacementEventData eventData = placementService.savePlacement(caseData);

        caseDetails.getData().put("placements", eventData.getPlacements());
        caseDetails.getData().put("placementIdToBeSealed", eventData.getPlacementIdToBeSealed());
        caseDetails.getData().put("placementsNonConfidential", eventData.getPlacementsNonConfidential(false));
        caseDetails.getData().put("placementsNonConfidentialNotices", eventData.getPlacementsNonConfidential(true));

        removeTemporaryFields(caseDetails, PlacementEventData.class);

        return respond(caseDetails.getData());
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest request) {

        CaseData caseData = getCaseData(request);
        final CaseData caseDataBefore = getCaseDataBefore(request);

        final PlacementEventData sealedEventData = placementService
            .sealPlacementApplicationAfterEventSubmitted(caseData);

        Map<String, Object> updates = new HashMap<>();
        if (sealedEventData != null) {
            if (isNotEmpty(sealedEventData.getPlacements())) {
                updates.put("placements", sealedEventData.getPlacements());
            }
            if (isNotEmpty(sealedEventData.getPlacement())) {
                updates.put("placement", sealedEventData.getPlacement());
            }
        }

        if (!updates.isEmpty()) {
            coreCaseDataService.triggerEvent(caseData.getId(),
                "internal-change-placement",
                updates);

            final CaseDetails caseDetails = request.getCaseDetails();
            caseDetails.getData().putAll(updates);

            caseData = getCaseData(caseDetails);
        }

        publishEvents(placementService.getEvents(caseData, caseDataBefore));
    }

    @PostMapping("/post-submit-callback/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handlePostSubmittedEvent(@RequestBody CallbackRequest request) {
        final CaseDetails caseDetails = request.getCaseDetails();

        caseDetails.getData().remove("placementIdToBeSealed");

        return respond(caseDetails);
    }

}
