package uk.gov.hmcts.reform.fpl.controllers.orders;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersRemovedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.removeorder.RemoveOrderService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;

@RestController
@RequestMapping("/callback/remove-draft-orders")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RemoveDraftOrdersController extends CallbackController {

    private final RemoveOrderService removeOrderService;
    private final ObjectMapper mapper;

    @PostMapping("/about-to-start")
    public CallbackResponse handlePopulateInitialData(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = CaseDetailsMap.caseDetailsMap(caseDetails);

        List<Element<HearingOrder>> allDraftOrders = removeOrderService.getDraftHearingOrders(caseData);

        caseDetailsMap.put("removableOrderList", asDynamicList(allDraftOrders, null, RemovableOrder::asLabel));

        return respond(caseDetailsMap);
    }

    @PostMapping("/mid-event")
    public CallbackResponse handlePopulateDraftInfo(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = CaseDetailsMap.caseDetailsMap(caseDetails);

        // When dynamic lists are fixed this can be moved into the below method
        UUID removedOrderId = getDynamicListSelectedValue(caseData.getRemovalToolData().getRemovableOrderList(),
            mapper);
        RemovableOrder removableOrder = removeOrderService.getRemovedOrderByUUID(caseData, removedOrderId);

        removeOrderService.populateSelectedOrderFields(caseData, caseDetailsMap, removedOrderId, removableOrder);

        return respond(caseDetailsMap);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);
        CaseData caseData = getCaseData(caseDetails);

        UUID removedOrderId = getDynamicListSelectedValue(caseData.getRemovalToolData().getRemovableOrderList(),
            mapper);
        RemovableOrder removableOrder = removeOrderService.getRemovedOrderByUUID(caseData, removedOrderId);

        removeOrderService.removeOrderFromCase(caseData, caseDetailsMap, removedOrderId, removableOrder);

        removeTemporaryFields(
            caseDetailsMap,
            "removableOrderList",
            "orderToBeRemoved",
            "orderTitleToBeRemoved",
            "showRemoveCMOFieldsFlag",
            "showReasonFieldFlag"
        );

        return respond(caseDetailsMap);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest request) {
        final CaseData caseDataBefore = getCaseDataBefore(request);
        final CaseData caseData = getCaseData(request);

        final List<Element<HearingOrder>> draftOrdersBefore = removeOrderService.getDraftHearingOrders(caseDataBefore);
        final List<Element<HearingOrder>> draftOrdersAfter = removeOrderService.getDraftHearingOrders(caseData);

        draftOrdersBefore.stream()
            .filter(draftOrder -> ElementUtils.findElement(draftOrder.getId(), draftOrdersAfter).isEmpty())
            .findFirst().ifPresent(hearingOrderElement ->
                publishEvent(new DraftOrdersRemovedEvent(caseData, caseDataBefore, hearingOrderElement,
                    caseData.getRemovalToolData().getReasonToRemoveOrder())));
    }
}
