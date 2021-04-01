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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.OrderDocumentGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.OrderPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.OrderShowHideQuestionsCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.OrderValidator;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.SECTION_2;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.SECTION_3;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.SECTION_4;

@Api
@RestController
@RequestMapping("/callback/manage-orders")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageOrdersController extends CallbackController {

    private final OrderValidator orderValidator;
    private final OrderShowHideQuestionsCalculator showHideQuestionsCalculator;
    private final OrderDocumentGenerator orderDocumentGenerator;
    private final ManageOrderDocumentScopedFieldsCalculator fieldsCalculator;
    private final OrderPrePopulator orderPrePopulator;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);

        return respond(caseDetails, List.of());
    }

    @PostMapping("/section-1/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleFinalOrderFlagsMidEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);
        Order order = Order.valueOf((String) data.get("manageOrdersType"));

        data.put("orderTempQuestions", showHideQuestionsCalculator.calculate(order));

        data.putAll(orderPrePopulator.prePopulate(order, SECTION_2, caseData, caseDetails));

        return respond(caseDetails);
    }


    @PostMapping("/section-2/mid-event")
    public AboutToStartOrSubmitCallbackResponse handlePopulateSelectorMidEvent(
        @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        Order order = Order.valueOf((String) data.get("manageOrdersType"));

        List<String> errors = orderValidator.validate(order, SECTION_2, caseDetails);

        if (errors.isEmpty()) {
            data.putAll(orderPrePopulator.prePopulate(order, SECTION_3, caseData, caseDetails));
        }

        return respond(caseDetails, errors);
    }

    @PostMapping("/section-3/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        Order order = Order.valueOf((String) data.get("manageOrdersType"));

        List<String> errors = orderValidator.validate(order, SECTION_3, caseDetails);

        if (errors.isEmpty()) {
            data.putAll(orderPrePopulator.prePopulate(order, SECTION_4, caseData, caseDetails));
        }

        return respond(caseDetails, errors);
    }

    @PostMapping("/section-4/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent4(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        Order order = Order.valueOf((String) data.get("manageOrdersType"));

        List<String> errors = orderValidator.validate(order, SECTION_4, caseDetails);

        return respond(caseDetails, errors);
    }


    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);

        Order order = Order.valueOf((String) data.get("manageOrdersType"));

        DocmosisDocument docmosisDocument = orderDocumentGenerator.generate(order, caseDetails);
        // upload and save

        fieldsCalculator.calculate().forEach(data::remove);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);

    }


}
