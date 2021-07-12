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
import uk.gov.hmcts.reform.fpl.events.order.ManageOrdersEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderOperationPostPopulator;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrdersEventBuilder;
import uk.gov.hmcts.reform.fpl.service.orders.OrderProcessingService;
import uk.gov.hmcts.reform.fpl.service.orders.OrderShowHideQuestionsCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.list.AmendableOrderListBuilder;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.OrderSectionAndQuestionsPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.modifier.ManageOrdersCaseDataFixer;
import uk.gov.hmcts.reform.fpl.service.orders.validator.OrderValidator;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Api
@RestController
@RequestMapping("/callback/manage-orders")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageOrdersController extends CallbackController {
    private final OrderValidator orderValidator;
    private final OrderShowHideQuestionsCalculator showHideQuestionsCalculator;
    private final ManageOrderDocumentScopedFieldsCalculator fieldsCalculator;
    private final OrderSectionAndQuestionsPrePopulator orderSectionAndQuestionsPrePopulator;
    private final OrderProcessingService orderProcessing;
    private final ManageOrderOperationPostPopulator operationPostPopulator;
    private final ManageOrdersCaseDataFixer manageOrdersCaseDataFixer;
    private final AmendableOrderListBuilder amendableOrderListBuilder;
    private final ManageOrdersEventBuilder eventBuilder;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put("manageOrdersAmendmentList", amendableOrderListBuilder.buildList(caseData));

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

        data.putAll(orderProcessing.process(caseData));

        fieldsCalculator.calculate().forEach(data::remove);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        // Do i need to check first
        publishEvent(eventBuilder.build(caseData, caseDataBefore).get());

    }

    private CaseData fixAndRetrieveCaseData(CaseDetails caseDetails) {
        return manageOrdersCaseDataFixer.fix(getCaseData(caseDetails));
    }

}
