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
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderRemovedEvent;
import uk.gov.hmcts.reform.fpl.events.cmo.CMORemovedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.RemoveOrderService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;

@Api
@RestController
@RequestMapping("/callback/remove-order")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RemoveOrderController extends CallbackController {
    private static final String REMOVABLE_ORDER_LIST_KEY = "removableOrderList";
    private final ObjectMapper mapper;
    private final RemoveOrderService service;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.put(REMOVABLE_ORDER_LIST_KEY, service.buildDynamicListOfOrders(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        // When dynamic lists are fixed this can be moved into the below method
        UUID id = getDynamicListSelectedValue(caseData.getRemovableOrderList(), mapper);

        data.putAll(service.populateSelectedOrderFields(caseData.getOrderCollection(), id));

        // Can be removed once dynamic lists are fixed
        data.put(REMOVABLE_ORDER_LIST_KEY, service.buildDynamicListOfOrders(caseData, id));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        UUID removedOrderId = getDynamicListSelectedValue(caseData.getRemovableOrderList(), mapper);
        RemovableOrder removableOrder = service.getRemovedOrderByUUID(caseData, removedOrderId);

        service.removeOrderFromCase(caseData, data, removedOrderId, removableOrder);

        removeTemporaryFields(
            caseDetails,
            REMOVABLE_ORDER_LIST_KEY,
            "reasonToRemoveOrder",
            "orderToBeRemoved",
            "orderTitleToBeRemoved",
            "orderIssuedDateToBeRemoved",
            "orderDateToBeRemoved"
        );

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData previousData = getCaseDataBefore(callbackRequest);
        CaseData caseData = getCaseData(callbackRequest);

        CaseManagementOrder removedCMO = getRemovedCMO(previousData.getSealedCMOs(), caseData.getSealedCMOs());

        if (removedCMO != null) {
            publishEvent(new CMORemovedEvent(caseData, removedCMO.getRemovalReason()));
        } else if (isSDORemoved(previousData, caseData)) {
            publishEvent(new StandardDirectionsOrderRemovedEvent(caseData, null));
            // TODO: set sdo removalReason
        }
    }

    private boolean isSDORemoved(CaseData previousData, CaseData caseData) {
        return previousData.getStandardDirectionOrder() != null && caseData.getStandardDirectionOrder() == null;
    }

    private CaseManagementOrder getRemovedCMO(
        List<Element<CaseManagementOrder>> previousSealedCMOs,
        List<Element<CaseManagementOrder>> newSealedCMOs
    ) {
        return previousSealedCMOs.stream()
            .filter(element -> !newSealedCMOs.contains(element))
            .findFirst()
            .map(Element::getValue).orElse(null);
    }

}
