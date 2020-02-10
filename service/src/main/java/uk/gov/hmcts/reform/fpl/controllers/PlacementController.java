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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.PlacementService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.NOTICE_OF_PLACEMENT_ORDER;

@Api
@RestController
@RequestMapping("/callback/placement")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlacementController {
    private final ObjectMapper mapper;
    private final PlacementService placementService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RequestData requestData;

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

        removeTemporaryFields(caseDetails, "placement", "placementChildName", "singleChild");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseProperties)
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody @NotNull CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        CaseData caseDataBefore = mapper.convertValue(caseDetailsBefore.getData(), CaseData.class);

        List<String> currentDocumentUrls = getBinaryUrlsForNoticeOfPlacementOrder(caseData);
        List<String> previousDocumentUrls = getBinaryUrlsForNoticeOfPlacementOrder(caseDataBefore);
        currentDocumentUrls.removeAll(previousDocumentUrls);

        if (!currentDocumentUrls.isEmpty()) {
            applicationEventPublisher.publishEvent(
                new NoticeOfPlacementOrderUploadedEvent(
                    callbackRequest,
                    requestData.authorisation(),
                    requestData.userId()));
        }
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

    private List<String> getBinaryUrlsForNoticeOfPlacementOrder(CaseData caseData) {
        return placementService.getBinaryUrlsForOrderAndNotices(
            caseData.getPlacements(), NOTICE_OF_PLACEMENT_ORDER);
    }
}
