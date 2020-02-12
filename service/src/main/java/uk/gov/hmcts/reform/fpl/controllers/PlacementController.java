package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.NoticeOfPlacementOrderUploadedEvent;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.PlacementService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.NOTICE_OF_PLACEMENT_ORDER;

@Api
@RestController
@RequestMapping("/callback/placement")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlacementController {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper mapper;
    private final PlacementService placementService;
    private final RequestData requestData;
    private final CoreCaseDataService coreCaseDataService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseProperties = caseDetails.getData();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        boolean singleChild = placementService.hasSingleChild(caseData);

        caseProperties.put("singleChild", YesNo.from(singleChild));

        if (singleChild) {
            Element<Child> child = caseData.getAllChildren().get(0);
            caseProperties.put("placement", placementService.getPlacement(caseData, child));
            caseProperties.put("placementChildName", child.getValue().getParty().getFullName());
        } else {
            caseProperties.put("childrenList", placementService.getChildrenList(caseData, null));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseProperties)
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseProperties = caseDetails.getData();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        UUID childId = getSelectedChildId(caseDetails, caseData);
        Element<Child> child = placementService.getChild(caseData, childId);

        caseProperties.put("childrenList", placementService.getChildrenList(caseData, child));
        caseProperties.put("placement", placementService.getPlacement(caseData, child));
        caseProperties.put("placementChildName", child.getValue().getParty().getFullName());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseProperties)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        Map<String, Object> caseProperties = caseDetails.getData();

        UUID childId = getSelectedChildId(caseDetails, caseData);
        Element<Child> child = placementService.getChild(caseData, childId);

        Placement placement = mapper.convertValue(caseDetails.getData().get("placement"), Placement.class)
            .setChild(child);

        List<Element<Placement>> updatedPlacement = placementService.setPlacement(caseData, placement);

        caseProperties.put("confidentialPlacements", updatedPlacement);
        caseProperties.put("placementsWithoutPlacementOrder", placementService.withoutPlacementOrder(updatedPlacement));
        caseProperties.put("placements", placementService.withoutConfidentialData(updatedPlacement));

        removeTemporaryFields(caseDetails, "placementChildName", "singleChild");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseProperties)
            .build();
    }

    //TODO: where should private methods for send notifications exist?
    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        CaseData caseDataBefore = mapper.convertValue(caseDetailsBefore.getData(), CaseData.class);

        sendNotificationForNewPlacementOrder(callbackRequest, caseDetails, caseData, caseDataBefore);
        sendNotificationForNewNoticeOfPlacementOrder(callbackRequest, caseData, caseDataBefore);
    }

    private void sendNotificationForNewPlacementOrder(CallbackRequest callbackRequest,
                                                      CaseDetails caseDetails,
                                                      CaseData caseData,
                                                      CaseData caseDataBefore) {
        UUID childId = getSelectedChildId(caseDetails, caseData);
        Element<Child> child = placementService.getChild(caseData, childId);

        Placement currentPlacement = placementService.getPlacement(caseData, child);
        Placement previousPlacement = placementService.getPlacement(caseDataBefore, child);

        // TODO refactor: this logic is confusing. Suggested: new method isNewOrUpdated...
        if (!isUpdatingExistingPlacement(previousPlacement, currentPlacement)) {
            publishPlacementApplicationUploadEvent(callbackRequest);
        }

        var placement = mapper.convertValue(caseDetails.getData().get("placement"), Placement.class);
        coreCaseDataService.triggerEvent(
            callbackRequest.getCaseDetails().getJurisdiction(),
            callbackRequest.getCaseDetails().getCaseTypeId(),
            callbackRequest.getCaseDetails().getId(),
            "internal-change:SEND_DOCUMENT",
            Map.of("documentToBeSent", placement.getApplication())
        );
    }

    private void sendNotificationForNewNoticeOfPlacementOrder(CallbackRequest callbackRequest,
                                                              CaseData caseData,
                                                              CaseData caseDataBefore) {
        List<String> currentDocumentUrls = getBinaryUrlsForNoticeOfPlacementOrder(caseData.getPlacements());
        List<String> previousDocumentUrls = getBinaryUrlsForNoticeOfPlacementOrder(caseDataBefore.getPlacements());
        currentDocumentUrls.removeAll(previousDocumentUrls);

        currentDocumentUrls.forEach(newDocument -> applicationEventPublisher.publishEvent(
            new NoticeOfPlacementOrderUploadedEvent(
                callbackRequest,
                requestData.authorisation(),
                requestData.userId())));
    }

    private UUID getSelectedChildId(CaseDetails caseDetails, CaseData caseData) {
        if (placementService.hasSingleChild(caseData)) {
            return caseData.getAllChildren().get(0).getId();
        }

        Object childrenList = caseDetails.getData().get("childrenList");

        //see RDM-5696
        if (childrenList instanceof String) {
            return UUID.fromString(childrenList.toString());
        }
        return mapper.convertValue(childrenList, DynamicList.class).getValueCode();
    }

    private void removeTemporaryFields(CaseDetails caseDetails, String... fields) {
        for (String field : fields) {
            caseDetails.getData().remove(field);
        }
    }

    private void publishPlacementApplicationUploadEvent(CallbackRequest callbackRequest) {
        applicationEventPublisher.publishEvent(
            new PlacementApplicationEvent(callbackRequest, requestData.authorisation(), requestData.userId()));
    }

    //TODO: refactor logic. Double negative for !isNotEmpty currently.
    private boolean isUpdatingExistingPlacement(Placement previousPlacement, Placement newPlacement) {
        return isNotEmpty(previousPlacement)
            && newPlacement.getApplication().equals(previousPlacement.getApplication());
    }

    private List<String> getBinaryUrlsForNoticeOfPlacementOrder(List<Element<Placement>> placements) {
        return placementService.getBinaryUrlsForOrderAndNotices(placements, NOTICE_OF_PLACEMENT_ORDER);
    }
}
