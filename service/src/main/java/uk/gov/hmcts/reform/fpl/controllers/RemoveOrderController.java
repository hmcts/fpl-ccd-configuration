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
import uk.gov.hmcts.reform.fpl.enums.OrderOrApplication;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderRemovedEvent;
import uk.gov.hmcts.reform.fpl.events.cmo.CMORemovedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.ApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.removeorder.RemovalService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.OrderOrApplication.APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.OrderOrApplication.ORDER;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;

@Api
@RestController
@RequestMapping("/callback/remove-order")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RemoveOrderController extends CallbackController {
    private static final String REMOVABLE_ORDER_LIST_KEY = "removableOrderList";
    private static final String REMOVABLE_APPLICATION_LIST_KEY = "removableApplicationList";
    private final ObjectMapper mapper;
    private final RemovalService service;

    public static final String CMO_ERROR_MESSAGE = "Email the help desk at dcd-familypubliclawservicedesk@hmcts.net to"
        + " remove this order. Quoting CMO %s, and the hearing it was added for.";

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = getCaseData(request.getCaseDetails());

        data.put(REMOVABLE_ORDER_LIST_KEY, service.buildDynamicListOfOrders(caseData));
        data.put(REMOVABLE_APPLICATION_LIST_KEY, caseData.buildApplicationBundlesDynamicList());

        return respond(data);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);
        CaseData caseData = getCaseData(caseDetails);

        if (caseData.getOrderOrApplication() == APPLICATION) {
            UUID removedApplicationId = getDynamicListSelectedValue(caseData.getRemovableApplicationList(), mapper);
            ApplicationsBundle bundle = service.getRemovedApplicationById(caseData, removedApplicationId);
            caseDetailsMap.putAll(service.populateApplicationFields(bundle));

            caseDetailsMap.put(REMOVABLE_APPLICATION_LIST_KEY,
                caseData.buildApplicationBundlesDynamicList(removedApplicationId));

        }
        else {
            // When dynamic lists are fixed this can be moved into the below method
            UUID removedOrderId = getDynamicListSelectedValue(caseData.getRemovableOrderList(), mapper);
            RemovableOrder removableOrder = service.getRemovedOrderByUUID(caseData, removedOrderId);

            service.populateSelectedOrderFields(caseData, caseDetailsMap, removedOrderId, removableOrder);

            // Can be removed once dynamic lists are fixed
            caseDetailsMap.put(REMOVABLE_ORDER_LIST_KEY, service.buildDynamicListOfOrders(caseData, removedOrderId));
        }
        return respond(caseDetailsMap);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);
        CaseData caseData = getCaseData(caseDetails);

        if (caseData.getOrderOrApplication() == APPLICATION) {
            UUID removedApplicationId = getDynamicListSelectedValue(caseData.getRemovableApplicationList(), mapper);
            ApplicationsBundle bundle = service.getRemovedApplicationById(caseData, removedApplicationId);

            service.removeApplicationFromCase(caseData, bundle);
        }
        else {
            UUID removedOrderId = getDynamicListSelectedValue(caseData.getRemovableOrderList(), mapper);
            RemovableOrder removableOrder = service.getRemovedOrderByUUID(caseData, removedOrderId);

            service.removeOrderFromCase(caseData, caseDetailsMap, removedOrderId, removableOrder);

        }
        removeTemporaryFields(
            caseDetailsMap,
            REMOVABLE_ORDER_LIST_KEY,
            "reasonToRemoveOrder",
            "reasonToRemoveApplication",
            "orderToBeRemoved",
            "applicationToBeRemoved",
            "orderTitleToBeRemoved",
            "applicationTypeToBeRemoved",
            "orderIssuedDateToBeRemoved",
            "orderDateToBeRemoved",
            "hearingToUnlink",
            "showRemoveCMOFieldsFlag",
            "showRemoveSDOWarningFlag",
            "showReasonFieldFlag"
        );

        return respond(caseDetailsMap);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        Optional<StandardDirectionOrder> removedSDO = service.getRemovedSDO(
            caseData.getHiddenStandardDirectionOrders(), caseDataBefore.getHiddenStandardDirectionOrders()
        );
        Optional<HearingOrder> removedCMO = service.getRemovedCMO(
            caseData.getHiddenCMOs(), caseDataBefore.getHiddenCMOs()
        );

        if (removedSDO.isPresent()) {
            publishEvent(new PopulateStandardDirectionsEvent(callbackRequest));
            publishEvent(new StandardDirectionsOrderRemovedEvent(
                caseData, removedSDO.map(StandardDirectionOrder::getRemovalReason).orElse("")));
        } else if (removedCMO.isPresent()) {
            publishEvent(
                new CMORemovedEvent(caseData, removedCMO.map(HearingOrder::getRemovalReason).orElse("")));
        }
    }
}
