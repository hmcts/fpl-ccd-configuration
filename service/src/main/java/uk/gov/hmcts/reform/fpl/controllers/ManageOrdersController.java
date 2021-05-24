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
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.OrderShowHideQuestionsCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.OrderSectionAndQuestionsPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.validator.OrderValidator;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;

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

    @PostMapping("/initial-selection/mid-event")
    public AboutToStartOrSubmitCallbackResponse populateInitialSection(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);

        if (caseData.getState() == State.CLOSED) {
            data.put("manageOrdersState", State.CLOSED);
            data.put("manageOrdersType", C21_BLANK_ORDER);
            data.put("orderTempQuestions", showHideQuestionsCalculator.calculate(C21_BLANK_ORDER));
            data.putAll(orderSectionAndQuestionsPrePopulator.prePopulate(
                C21_BLANK_ORDER, C21_BLANK_ORDER.firstSection(), getCaseData(caseDetails)));
        }

        return respond(caseDetails);
    }

    @PostMapping("/order-selection/mid-event")
    public AboutToStartOrSubmitCallbackResponse prepareQuestions(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);

        Order order = caseData.getManageOrdersEventData().getManageOrdersType();

        data.put("orderTempQuestions", showHideQuestionsCalculator.calculate(order));

        data.putAll(orderSectionAndQuestionsPrePopulator.prePopulate(order, order.firstSection(), caseData));

        return respond(caseDetails);
    }

    @PostMapping("/{section}/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleSectionMidEvent(@PathVariable String section,
                                                                      @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
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
        CaseData caseData = getCaseData(caseDetails);

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

}
