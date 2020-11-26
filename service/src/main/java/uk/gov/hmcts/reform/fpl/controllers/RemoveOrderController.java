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
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.service.removeorder.RemoveOrderService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
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
        CaseData caseData = getCaseData(request.getCaseDetails());

        data.put(REMOVABLE_ORDER_LIST_KEY, service.buildDynamicListOfOrders(caseData));

        return respond(data);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);
        CaseData caseData = getCaseData(caseDetails);

        // When dynamic lists are fixed this can be moved into the below method
        UUID removedOrderId = getDynamicListSelectedValue(caseData.getRemovableOrderList(), mapper);
        RemovableOrder removableOrder = service.getRemovedOrderByUUID(caseData, removedOrderId);

        service.populateSelectedOrderFields(caseData, caseDetailsMap, removedOrderId, removableOrder);

        // Can be removed once dynamic lists are fixed
        caseDetailsMap.put(REMOVABLE_ORDER_LIST_KEY, service.buildDynamicListOfOrders(caseData, removedOrderId));

        return respond(caseDetailsMap);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);
        CaseData caseData = getCaseData(caseDetails);

        UUID removedOrderId = getDynamicListSelectedValue(caseData.getRemovableOrderList(), mapper);
        RemovableOrder removableOrder = service.getRemovedOrderByUUID(caseData, removedOrderId);

        service.removeOrderFromCase(caseData, caseDetailsMap, removedOrderId, removableOrder);

        removeTemporaryFields(
            caseDetailsMap,
            REMOVABLE_ORDER_LIST_KEY,
            "reasonToRemoveOrder",
            "orderToBeRemoved",
            "orderTitleToBeRemoved",
            "orderIssuedDateToBeRemoved",
            "orderDateToBeRemoved",
            "hearingToUnlink",
            "showRemoveCMOFieldsFlag",
            "showRemoveSDOWarningFlag"
        );

        return respond(caseDetailsMap);
    }
}
