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
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.CloseCase;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.DischargeCareOrderService;
import uk.gov.hmcts.reform.fpl.service.GeneratedOrderService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.validation.groups.ValidateFamilyManCaseNumberGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.epoordergroup.EPOAddressGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey.CARE_ORDER_SELECTOR;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey.MULTIPLE_CARE_ORDER_LABEL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey.SINGLE_CARE_ORDER_LABEL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.DISCHARGE_OF_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.CloseCaseReason.FINAL_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.removeAllocatedJudgeProperties;

@Api
@RestController
@RequestMapping("/callback/create-order")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GeneratedOrderController extends CallbackController {

    private final Time time;
    private final GeneratedOrderService service;
    private final ValidateGroupService validateGroupService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final ChildrenService childrenService;
    private final DischargeCareOrderService dischargeCareOrder;
    private final ValidateEmailService validateEmailService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = validateGroupService.validateGroup(caseData, ValidateFamilyManCaseNumberGroup.class);

        if (errors.isEmpty()) {
            data.put("pageShow", caseData.getAllChildren().size() <= 1 ? "No" : "Yes");

            data.put("dateOfIssue", time.now().toLocalDate());
            data.put("dateAndTimeOfIssue", time.now().toLocalDate().atStartOfDay());

            if (caseData.getAllocatedJudge() != null) {
                data.put("judgeAndLegalAdvisor", setAllocatedJudgeLabel(caseData.getAllocatedJudge()));
            }

            if (CLOSED.getValue().equals(caseDetails.getState())) {
                data.put("orderTypeAndDocument", OrderTypeAndDocument.builder().type(BLANK_ORDER).build());
            }
        }

        return respond(caseDetails, errors);
    }

    @PostMapping("/prepare-selected-order/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleFinalOrderFlagsMidEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);
        final OrderTypeAndDocument currentOrder = caseData.getOrderTypeAndDocument();

        if (DISCHARGE_OF_CARE_ORDER == currentOrder.getType()) {
            final List<String> errors = new ArrayList<>();
            List<GeneratedOrder> careOrders = dischargeCareOrder.getCareOrders(caseData);

            if (careOrders.isEmpty()) {
                errors.add("No care orders to be discharged");
            } else if (careOrders.size() == 1) {
                data.put(SINGLE_CARE_ORDER_LABEL.getKey(), dischargeCareOrder.getOrdersLabel(careOrders));
            } else {
                data.put(CARE_ORDER_SELECTOR.getKey(), newSelector(careOrders.size()));
                data.put(MULTIPLE_CARE_ORDER_LABEL.getKey(), dischargeCareOrder.getOrdersLabel(careOrders));
            }

            return respond(caseDetails, errors);
        }

        List<Element<Child>> children = caseData.getAllChildren();

        if (!service.isFinalOrderAllowed(caseData.getOrderTypeAndDocument(), children)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(data)
                .errors(List.of("All children in the case already have final orders"))
                .build();
        }

        if (caseData.getOrderTypeAndDocument().isClosable()) {
            Optional<Integer> remainingChildIndex = childrenService.getRemainingChildIndex(children);
            if (remainingChildIndex.isPresent()) {
                data.put("remainingChildIndex", String.valueOf(remainingChildIndex.get()));
                data.put("remainingChild",
                    childrenService.getRemainingChildrenNames(children));
                data.put("otherFinalOrderChildren",
                    childrenService.getFinalOrderIssuedChildrenNames(children));
                data.put("showFinalOrderSingleChildPage", "Yes");
            }
        }

        return respond(caseDetails);
    }

    @PostMapping("/populate-children-selector/mid-event")
    public AboutToStartOrSubmitCallbackResponse handlePopulateSelectorMidEvent(
        @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        if (NO.getValue().equals(caseData.getOrderAppliesToAllChildren())) {
            final Selector childSelector = newSelector(caseData.getAllChildren().size());
            boolean closable = caseData.getOrderTypeAndDocument().isClosable();

            if (closable) {
                childSelector.setHidden(childrenService.getIndexesOfChildrenWithFinalOrderIssued(caseData));
            }
            caseDetails.getData().put("childSelector", childSelector);
            caseDetails.getData().put("children_label",
                childrenService.getChildrenLabel(caseData.getAllChildren(), closable));
        }

        return respond(caseDetails);
    }

    /*
     This mid event is called after:
      • Inputting Judge + LA
      • Adding further directions
      • Uploading an order
    */
    @PostMapping("/generate-document/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);
        OrderTypeAndDocument orderTypeAndDocument = caseData.getOrderTypeAndDocument();

        JudgeAndLegalAdvisor tempJudge = caseData.getJudgeAndLegalAdvisor();

        if (!orderTypeAndDocument.isUploaded() && caseData.hasSelectedTemporaryJudge(tempJudge)) {
            Optional<String> error = validateEmailService.validate(tempJudge.getJudgeEmailAddress());

            if (!error.isEmpty()) {
                return respond(caseDetails, List.of(error.get()));
            }
        }

        FurtherDirections orderFurtherDirections = caseData.getOrderFurtherDirections();
        List<Element<Child>> children;

        if (orderTypeAndDocument.isClosable()) {
            children = getUpdatedChildren(caseData);
        } else {
            children = caseData.getAllChildren();
        }

        // If can display close case, set the flag in order to show the close case page
        if (service.showCloseCase(orderTypeAndDocument, children)) {
            data.put("showCloseCaseFromOrderPage", YES);
            data.put("close_case_label", CloseCaseController.LABEL);
        } else {
            data.put("showCloseCaseFromOrderPage", NO);
        }

        if (service.shouldGenerateDocument(orderTypeAndDocument, orderFurtherDirections)) {
            Document document = getDocument(caseData, DRAFT);

            //Update orderTypeAndDocument with the document so it can be displayed in check-your-answers
            data.put("orderTypeAndDocument", service.buildOrderTypeAndDocument(orderTypeAndDocument, document));
        } else if (orderTypeAndDocument.isUploaded()) {
            // check yo order
            data.putAll(Map.of(
                "readOnlyFamilyManCaseNumber", caseData.getFamilyManCaseNumber(),
                "readOnlyChildren", childrenService.getSelectedChildren(caseData).stream()
                    .map(child -> child.getValue().getParty().getFullName())
                    .collect(joining("\n")),
                "readOnlyOrder", caseData.getUploadedOrder()
            ));
        }

        return respond(caseDetails);
    }

    @PostMapping("/populate-epo-parameters/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEventEPOExclusionRequirement(
        @RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        final List<String> errors = validateGroupService.validateGroup(caseData, EPOAddressGroup.class);

        data.put("epoWhoIsExcluded", caseData.getOrders().getExcluded());
        data.put("epoType", caseData.getOrders().getEpoType());
        data.put("epoRemovalAddress", caseData.getOrders().getAddress());

        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);

        List<Element<GeneratedOrder>> orders = caseData.getOrderCollection();
        OrderTypeAndDocument typeAndDocument = caseData.getOrderTypeAndDocument();

        if (!typeAndDocument.isUploaded()) {
            JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(
                caseData.getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge()
            );

            Document document = getDocument(caseData, SEALED);

            OrderTypeAndDocument orderTypeAndDocument = service.buildOrderTypeAndDocument(typeAndDocument, document);

            removeAllocatedJudgeProperties(judgeAndLegalAdvisor);

            orders.add(element(service.buildCompleteOrder(orderTypeAndDocument, judgeAndLegalAdvisor, caseData)));
        } else {
            typeAndDocument.setDocument(caseData.getUploadedOrder());

            orders.add(element(service.buildCompleteOrder(typeAndDocument, null, caseData)));
        }

        data.put("orderCollection", orders);

        if (typeAndDocument.isClosable()) {
            data.put("children1", getUpdatedChildren(caseData));
        }

        if (caseData.isClosedFromOrder()) {
            data.put("state", CLOSED);
            data.put("closeCaseTabField", CloseCase.builder()
                .date(time.now().toLocalDate())
                .showFullReason(YES)
                .reason(FINAL_ORDER)
                .build());
        }

        service.removeOrderProperties(data);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);
        DocumentReference mostRecentUploadedDocument = service.getMostRecentUploadedOrderDocument(
            caseData.getOrderCollection());

        publishEvent(new GeneratedOrderEvent(caseData, mostRecentUploadedDocument));
    }

    private JudgeAndLegalAdvisor setAllocatedJudgeLabel(Judge allocatedJudge) {
        String assignedJudgeLabel = buildAllocatedJudgeLabel(allocatedJudge);

        return JudgeAndLegalAdvisor.builder()
            .allocatedJudgeLabel(assignedJudgeLabel)
            .build();
    }

    private Document getDocument(CaseData caseData,
                                 OrderStatus orderStatus) {

        OrderTypeAndDocument typeAndDoc = caseData.getOrderTypeAndDocument();

        caseData.setGeneratedOrderStatus(orderStatus);
        DocmosisGeneratedOrder orderTemplateData = service.getOrderTemplateData(caseData);

        DocmosisDocument docmosisDocument = docmosisDocumentGeneratorService.generateDocmosisDocument(
            orderTemplateData, typeAndDoc.getDocmosisTemplate());

        Document document = uploadDocumentService.uploadPDF(docmosisDocument.getBytes(),
            service.generateOrderDocumentFileName(typeAndDoc.getType(), typeAndDoc.getSubtype()));

        if (orderStatus == DRAFT) {
            document.originalDocumentName = "draft-" + document.originalDocumentName;
        }

        return document;
    }

    private List<Element<Child>> getUpdatedChildren(CaseData caseData) {
        List<Element<Child>> selectedChildren = childrenService.getSelectedChildren(caseData);//TODO - smart selector?
        return childrenService.updateFinalOrderIssued(
            caseData.getOrderTypeAndDocument().getTypeLabel(),
            caseData.getAllChildren(),
            caseData.getOrderAppliesToAllChildren(),
            selectedChildren
        );
    }
}
