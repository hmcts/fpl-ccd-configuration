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
import uk.gov.hmcts.reform.fpl.events.ApplicationFormRemovedEvent;
import uk.gov.hmcts.reform.fpl.events.ApplicationRemovedEvent;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderRemovedEvent;
import uk.gov.hmcts.reform.fpl.events.cmo.CMORemovedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RemovalToolData;
import uk.gov.hmcts.reform.fpl.model.RemovedApplicationForm;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.removeorder.RemoveApplicationService;
import uk.gov.hmcts.reform.fpl.service.removeorder.RemoveOrderService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.RemovableType.ADDITIONAL_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.RemovableType.APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.RemovableType.ORDER;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;

@Api
@RestController
@RequestMapping("/callback/remove-order")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RemovalToolController extends CallbackController {
    private static final String REMOVABLE_ORDER_LIST_KEY = "removableOrderList";
    private static final String REMOVABLE_APPLICATION_LIST_KEY = "removableApplicationList";
    private final ObjectMapper mapper;
    private final RemoveOrderService orderService;
    private final RemoveApplicationService applicationService;

    public static final String CMO_ERROR_MESSAGE = "Email the help desk at dcd-familypubliclawservicedesk@hmcts.net to"
        + " remove this order. Quoting CMO %s, and the hearing it was added for.";
    public static final String APPLICATION_FORM_ALREADY_REMOVED_ERROR_MESSAGE = "The application form for this case has"
        + " already been removed.";

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = getCaseData(request.getCaseDetails());

        data.put(REMOVABLE_ORDER_LIST_KEY, orderService.buildDynamicListOfOrders(caseData));
        data.put(REMOVABLE_APPLICATION_LIST_KEY, applicationService.buildDynamicList(caseData));

        return respond(data);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);
        CaseData caseData = getCaseData(caseDetails);

        if (caseData.getRemovalToolData().getRemovableType() == ADDITIONAL_APPLICATION) {
            UUID removedApplicationId = getDynamicListSelectedValue(
                caseData.getRemovalToolData().getRemovableApplicationList(), mapper);
            AdditionalApplicationsBundle application = applicationService.getRemovedApplicationById(
                caseData, removedApplicationId).getValue();

            applicationService.populateApplicationFields(caseDetailsMap, application);

            // Can be removed once dynamic lists are fixed
            caseDetailsMap.put(REMOVABLE_APPLICATION_LIST_KEY,
                applicationService.buildDynamicList(caseData, removedApplicationId));
        } else if (caseData.getRemovalToolData().getRemovableType() == ORDER) {
            // When dynamic lists are fixed this can be moved into the below method
            UUID removedOrderId = getDynamicListSelectedValue(caseData.getRemovalToolData().getRemovableOrderList(),
                mapper);
            RemovableOrder removableOrder = orderService.getRemovedOrderByUUID(caseData, removedOrderId);

            orderService.populateSelectedOrderFields(caseData, caseDetailsMap, removedOrderId, removableOrder);

            // Can be removed once dynamic lists are fixed
            caseDetailsMap.put(REMOVABLE_ORDER_LIST_KEY,
                orderService.buildDynamicListOfOrders(caseData, removedOrderId));
        } else if (caseData.getRemovalToolData().getRemovableType() == APPLICATION
            && isEmpty(caseData.getC110A().getDocument())) {
            return respond(caseDetailsMap, List.of(APPLICATION_FORM_ALREADY_REMOVED_ERROR_MESSAGE));
        }
        return respond(caseDetailsMap);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);
        CaseData caseData = getCaseData(caseDetails);

        if (caseData.getRemovalToolData().getRemovableType() == ADDITIONAL_APPLICATION) {
            UUID removedApplicationId = getDynamicListSelectedValue(
                caseData.getRemovalToolData().getRemovableApplicationList(), mapper);
            applicationService.removeApplicationFromCase(caseData, caseDetailsMap, removedApplicationId);
        } else if (caseData.getRemovalToolData().getRemovableType() == ORDER) {
            UUID removedOrderId = getDynamicListSelectedValue(caseData.getRemovalToolData().getRemovableOrderList(),
                mapper);
            RemovableOrder removableOrder = orderService.getRemovedOrderByUUID(caseData, removedOrderId);

            orderService.removeOrderFromCase(caseData, caseDetailsMap, removedOrderId, removableOrder);
        } else {
            if (isEmpty(caseData.getC110A().getDocument())) {
                return respond(caseDetailsMap, List.of(APPLICATION_FORM_ALREADY_REMOVED_ERROR_MESSAGE));
            }
            caseDetailsMap.put("submittedForm", null);
            caseDetailsMap.put("supplementDocument", null);
            caseDetailsMap.put("hiddenApplicationForm", RemovedApplicationForm.builder()
                    .submittedForm(caseData.getC110A().getDocument())
                    .submittedSupplement(!isEmpty(caseData.getC110A().getSupplementDocument())
                        ? caseData.getC110A().getSupplementDocument() : null)
                    .removalReason(caseData.getRemovalToolData().getReasonToRemoveApplicationForm())
                .build());
        }
        removeTemporaryFields(caseDetailsMap, RemovalToolData.temporaryFields());

        return respond(caseDetailsMap);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        Optional<StandardDirectionOrder> removedSDO = orderService.getRemovedSDO(
            caseData.getRemovalToolData().getHiddenStandardDirectionOrders(),
            caseDataBefore.getRemovalToolData().getHiddenStandardDirectionOrders()
        );
        Optional<HearingOrder> removedCMO = orderService.getRemovedCMO(
            caseData.getRemovalToolData().getHiddenCMOs(), caseDataBefore.getRemovalToolData().getHiddenCMOs()
        );
        Optional<AdditionalApplicationsBundle> removedApplication = applicationService.getRemovedApplications(caseData
            .getRemovalToolData().getHiddenApplicationsBundle(),
            caseDataBefore.getRemovalToolData().getHiddenApplicationsBundle());

        Optional<DocumentReference> removedApplicationForm =
            (caseData.getC110A().getSubmittedForm() != caseDataBefore.getC110A().getSubmittedForm())
            ? Optional.of(caseDataBefore.getC110A().getSubmittedForm()) : Optional.empty();

        if (removedSDO.isPresent()) {
            publishEvent(new PopulateStandardDirectionsEvent(callbackRequest));
            publishEvent(new StandardDirectionsOrderRemovedEvent(
                caseData, removedSDO.map(StandardDirectionOrder::getRemovalReason).orElse("")));
        } else if (removedCMO.isPresent()) {
            publishEvent(
                new CMORemovedEvent(caseData, removedCMO.map(HearingOrder::getRemovalReason).orElse("")));
        } else if (removedApplication.isPresent()) {
            publishEvent(new ApplicationRemovedEvent(caseData, removedApplication.get()));
        } else if (removedApplicationForm.isPresent()) {
            publishEvent(new ApplicationFormRemovedEvent(caseData, removedApplicationForm.get()));
        }
    }
}
