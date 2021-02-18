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
import uk.gov.hmcts.reform.fpl.events.NoticeOfPlacementOrderUploadedEvent;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.PlacementService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@Api
@RestController
@RequestMapping("/callback/placement")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlacementController extends CallbackController {
    private static final String PLACEMENT = "placement";
    private static final String PLACEMENT_CHILD_NAME = "placementChildName";
    private static final String CHILDREN_LIST = "childrenList";
    private final ObjectMapper mapper;
    private final PlacementService placementService;
    private final CoreCaseDataService coreCaseDataService;
    private final DocumentDownloadService documentDownloadService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseProperties = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);

        boolean singleChild = placementService.hasSingleChild(caseData);

        caseProperties.put("singleChild", YesNo.from(singleChild));

        if (singleChild) {
            Element<Child> child = caseData.getAllChildren().get(0);
            caseProperties.put(PLACEMENT, placementService.getPlacement(caseData, child));
            caseProperties.put(PLACEMENT_CHILD_NAME, child.getValue().getParty().getFullName());
        } else {
            caseProperties.put(CHILDREN_LIST, placementService.getChildrenList(caseData, null));
        }

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseProperties = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);

        UUID childId = getSelectedChildId(caseDetails, caseData);
        Element<Child> child = placementService.getChild(caseData, childId);

        caseProperties.put(CHILDREN_LIST, placementService.getChildrenList(caseData, child));
        caseProperties.put(PLACEMENT, placementService.getPlacement(caseData, child));
        caseProperties.put(PLACEMENT_CHILD_NAME, child.getValue().getParty().getFullName());

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> caseProperties = caseDetails.getData();

        UUID childId = getSelectedChildId(caseDetails, caseData);
        Element<Child> child = placementService.getChild(caseData, childId);

        Placement placement = mapper.convertValue(caseDetails.getData().get(PLACEMENT), Placement.class)
            .setChild(child);

        List<Element<Placement>> updatedPlacement = placementService.setPlacement(caseData, placement);

        caseProperties.put("confidentialPlacements", updatedPlacement);
        caseProperties.put("placementsWithoutPlacementOrder", placementService.withoutPlacementOrder(updatedPlacement));
        caseProperties.put("placements", placementService.withoutConfidentialData(updatedPlacement));

        removeTemporaryFields(caseDetails, PLACEMENT, PLACEMENT_CHILD_NAME, "singleChild");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseProperties)
            .build();
    }

    //TODO: where should private methods for send notifications exist? FPLA-1472
    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        sendNotificationForNewPlacementOrder(caseDetails, caseData, caseDataBefore);
        sendNotificationForNewNoticeOfPlacementOrder(caseData, caseDataBefore);
        triggerSendDocumentEventForUpdatedPlacementOrderDocuments(caseDetails, caseData, caseDataBefore);
    }

    private void sendNotificationForNewPlacementOrder(CaseDetails caseDetails,
                                                      CaseData caseData,
                                                      CaseData caseDataBefore) {
        UUID childId = getSelectedChildId(caseDetails, caseData);
        Element<Child> child = placementService.getChild(caseData, childId);

        Placement currentPlacement = placementService.getPlacement(caseData, child);
        Placement previousPlacement = placementService.getPlacement(caseDataBefore, child);

        if (isEmpty(previousPlacement) || isUpdatedPlacement(previousPlacement, currentPlacement)) {
            publishEvent(new PlacementApplicationEvent(caseData));
        }
    }

    private void sendNotificationForNewNoticeOfPlacementOrder(CaseData caseData, CaseData caseDataBefore) {
        placementService.getUpdatedDocuments(caseData, caseDataBefore, NOTICE_OF_PLACEMENT_ORDER)
            .stream()
            .map(documentReference -> new NoticeOfPlacementOrderUploadedEvent(caseData, documentReference))
            .forEach(this::publishEvent);
    }

    private void triggerSendDocumentEventForUpdatedPlacementOrderDocuments(CaseDetails caseDetails, CaseData caseData,
                                                                           CaseData caseDataBefore) {
        placementService.getUpdatedDocuments(caseData, caseDataBefore, PLACEMENT_ORDER)
            .forEach(documentReference -> coreCaseDataService.triggerEvent(
                caseDetails.getJurisdiction(),
                caseDetails.getCaseTypeId(),
                caseDetails.getId(),
                "internal-change-SEND_DOCUMENT",
                Map.of("documentToBeSent", documentReference)));
    }

    private UUID getSelectedChildId(CaseDetails caseDetails, CaseData caseData) {
        if (placementService.hasSingleChild(caseData)) {
            return caseData.getAllChildren().get(0).getId();
        }

        Object childrenList = caseDetails.getData().get(CHILDREN_LIST);

        //see RDM-5696
        if (childrenList instanceof String) {
            return UUID.fromString(childrenList.toString());
        }
        return mapper.convertValue(childrenList, DynamicList.class).getValueCodeAsUUID();
    }

    private boolean isUpdatedPlacement(Placement previousPlacement, Placement newPlacement) {
        return notEqual(newPlacement.getApplication(), previousPlacement.getApplication());
    }
}
