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
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.OrderShowHideQuestionsCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.OrderDocumentGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.OrderSectionAndQuestionsPrePopulator;
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
    private final OrderDocumentGenerator orderDocumentGenerator;
    private final ManageOrderDocumentScopedFieldsCalculator fieldsCalculator;
    private final OrderSectionAndQuestionsPrePopulator orderSectionAndQuestionsPrePopulator;

    @PostMapping("/section-1/mid-event")
    public AboutToStartOrSubmitCallbackResponse prepareQuestions(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);

        Order order = caseData.getManageOrdersEventData().getManageOrdersType();

        data.put("orderTempQuestions", showHideQuestionsCalculator.calculate(order));

        data.putAll(orderSectionAndQuestionsPrePopulator.prePopulate(
            order, order.getQuestions().get(0).getSection(), caseData, caseDetails
        ));

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

        List<String> errors = orderValidator.validate(order, currentSection, caseDetails);

        order.nextSection(currentSection).ifPresent(
            nextSection -> {
                if (errors.isEmpty()) {
                    data.putAll(
                        orderSectionAndQuestionsPrePopulator.prePopulate(order, nextSection, caseData, caseDetails));
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

        Order order = caseData.getManageOrdersEventData().getManageOrdersType();

        DocmosisDocument docmosisDocument = orderDocumentGenerator.generate(order, caseDetails, OrderStatus.SEALED);
        // TODO: 01/04/2021 upload to dm store

        // TODO: 01/04/2021 create object to store doc and other details in

        fieldsCalculator.calculate().forEach(data::remove);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);

        // TODO: 06/04/2021 get this correctly
        DocumentReference order = DocumentReference.builder().build();

        publishEvent(new GeneratedOrderEvent(caseData, order));
    }

}
