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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.OrderDocumentGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.OrderPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.OrderSectionLifeCycle;
import uk.gov.hmcts.reform.fpl.service.orders.OrderShowHideQuestionsCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.OrderValidator;

import java.util.ArrayList;
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
    private final OrderPrePopulator orderPrePopulator;
    private final OrderSectionLifeCycle sectionLifeCycle;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);

        // TODO: 01/04/2021 can probs be removed

        return respond(caseDetails, List.of());
    }

    @PostMapping("/section-1/mid-event")
    public AboutToStartOrSubmitCallbackResponse prepareQuestions(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);
        Order order = Order.valueOf((String) data.get("manageOrdersType"));

        data.put("orderTempQuestions", showHideQuestionsCalculator.calculate(order));

        data.putAll(
            orderPrePopulator.prePopulate(order, order.getQuestions().get(0).getSection(), caseData, caseDetails));

        return respond(caseDetails);
    }

    @PostMapping("/{section}/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleSectionMidEvent(@PathVariable String section,
                                                                      @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        Order order = Order.valueOf((String) data.get("manageOrdersType"));

        OrderSection currentSection = OrderSection.from(section);
        OrderSection nextSection = sectionLifeCycle.calculateNextSection(currentSection, order);

        List<String> errors = new ArrayList<>();

        if (currentSection.shouldValidate()) {
            errors = orderValidator.validate(order, currentSection, caseDetails);
        }

        if (errors.isEmpty()) {
            if (nextSection != null) {
                if (nextSection.shouldPrePopulate()) {
                    data.putAll(orderPrePopulator.prePopulate(order, nextSection, caseData, caseDetails));
                }
            } else {
                // next section is null that means that we have reached the end and want to generate the draft order
                // TODO: 01/04/2021 generate draft order in some way
                DocmosisDocument draftOrder = orderDocumentGenerator.generate(order, caseDetails);

                // add to case details to display as draft
            }
        }

        return respond(caseDetails, errors);
    }


    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);

        Order order = Order.valueOf((String) data.get("manageOrdersType"));

        DocmosisDocument docmosisDocument = orderDocumentGenerator.generate(order, caseDetails);
        // TODO: 01/04/2021 upload to dm store

        // TODO: 01/04/2021 create object to store doc and other details in

        fieldsCalculator.calculate().forEach(data::remove);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);

        // TODO: 01/04/2021 check if any notifications need to be sent out as part of this story

    }

}
