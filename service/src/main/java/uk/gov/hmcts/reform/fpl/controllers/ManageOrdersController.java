package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderOperationPostPopulator;
import uk.gov.hmcts.reform.fpl.service.orders.OrderShowHideQuestionsCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.AmendableOrderListBuilder;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.OrderSectionAndQuestionsPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.modifier.ManageOrdersCaseDataFixer;
import uk.gov.hmcts.reform.fpl.service.orders.validator.OrderValidator;

import java.util.List;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/manage-orders")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageOrdersController extends CallbackController {
    private final OrderValidator orderValidator;
    private final OrderShowHideQuestionsCalculator showHideQuestionsCalculator;
    private final ManageOrderDocumentScopedFieldsCalculator fieldsCalculator;
    private final OrderSectionAndQuestionsPrePopulator orderSectionAndQuestionsPrePopulator;
    private final SealedOrderHistoryService sealedOrderHistoryService;
    private final ManageOrderOperationPostPopulator operationPostPopulator;
    private final ManageOrdersCaseDataFixer manageOrdersCaseDataFixer;
    private final AmendableOrderListBuilder amendableOrderListBuilder;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        amendableOrderListBuilder.buildList(caseData).ifPresent(list ->
            caseDetails.getData().put("manageOrdersAmendmentList", list)
        );

        return respond(caseDetails);
    }

    @PostMapping("/initial-selection/mid-event")
    public AboutToStartOrSubmitCallbackResponse populateInitialSection(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        caseDetails.getData().putAll(operationPostPopulator.populate(caseDetails));

        return respond(caseDetails);
    }

    @PostMapping("/order-selection/mid-event")
    public AboutToStartOrSubmitCallbackResponse prepareQuestions(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = fixAndRetrieveCaseData(caseDetails);

        Order order = caseData.getManageOrdersEventData().getManageOrdersType();

        data.put("orderTempQuestions", showHideQuestionsCalculator.calculate(order));

        data.putAll(
            orderSectionAndQuestionsPrePopulator.prePopulate(
                order, order.firstSection(), fixAndRetrieveCaseData(caseDetails))
        );

        return respond(caseDetails);
    }

    @PostMapping("/{section}/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleSectionMidEvent(@PathVariable String section,
                                                                      @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = fixAndRetrieveCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        Order order = caseData.getManageOrdersEventData().getManageOrdersType();

        OrderSection currentSection = OrderSection.from(section);

        List<String> errors = orderValidator.validate(order, currentSection, caseData);

        order.nextSection(currentSection).ifPresent(
            nextSection -> {
                if (errors.isEmpty()) {
                    data.putAll(orderSectionAndQuestionsPrePopulator.prePopulate(order, nextSection, caseData));
                }
            }
        );

        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = fixAndRetrieveCaseData(caseDetails);

        data.putAll(sealedOrderHistoryService.generate(caseData));

        fieldsCalculator.calculate().forEach(data::remove);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);

        GeneratedOrder lastGeneratedOrder = sealedOrderHistoryService.lastGeneratedOrder(caseData);

        publishEvent(new GeneratedOrderEvent(caseData, lastGeneratedOrder.getDocument()));
    }

    private CaseData fixAndRetrieveCaseData(CaseDetails caseDetails) {
        return manageOrdersCaseDataFixer.fix(getCaseData(caseDetails));
    }

}
