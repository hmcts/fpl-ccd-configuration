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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
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
public class RemoveOrderController {
    private static final String REMOVABLE_ORDER_LIST_KEY = "removableOrderList";
    private static final String CMO_TYPE_KEY = "Case management order";
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

        List<Element<GeneratedOrder>> generatedOrders = caseData.getOrderCollection();
        List<Element<GeneratedOrder>> hiddenGeneratedOrders = caseData.getHiddenOrders();
        String reasonToRemoveOrder = caseData.getReasonToRemoveOrder();

        UUID removedOrderId = getDynamicListSelectedValue(caseData.getRemovableOrderList(), mapper);
        RemovableOrder removableOrder = service.getRemovedOrderByUUID(caseData, removedOrderId);

        if (isRemovingCMO(removableOrder)) {
            List<Element<CaseManagementOrder>> sealedCMOs = caseData.getSealedCMOs();
            List<Element<CaseManagementOrder>> hiddenCMOs = caseData.getHiddenCMOs();
            
            service.hideOrder(
                sealedCMOs, hiddenCMOs, caseData.getRemovableOrderList(), reasonToRemoveOrder
            );

        } else {
            // Removing generated order
            data.put("children1", service.removeFinalOrderPropertiesFromChildren(caseData));

            service.hideOrder(
                generatedOrders, hiddenGeneratedOrders, caseData.getRemovableOrderList(), reasonToRemoveOrder
            );
        }

        data.put("orderCollection", generatedOrders);
        data.put("hiddenOrders", hiddenGeneratedOrders);
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

    private boolean isRemovingCMO(RemovableOrder removableOrder) {
        return CMO_TYPE_KEY.equals(removableOrder.getType());
    }
}
