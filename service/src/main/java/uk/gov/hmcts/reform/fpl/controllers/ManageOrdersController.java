package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderOperationPostPopulator;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderPostSubmitHelper;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrdersEventBuilder;
import uk.gov.hmcts.reform.fpl.service.orders.OrderProcessingService;
import uk.gov.hmcts.reform.fpl.service.orders.OrderShowHideQuestionsCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.list.AmendableOrderListBuilder;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.OrderSectionAndQuestionsPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.modifier.ManageOrdersCaseDataFixer;
import uk.gov.hmcts.reform.fpl.service.orders.validator.OrderValidator;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@RestController
@RequestMapping("/callback/manage-orders")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class ManageOrdersController extends CallbackController {
    private final OrderValidator orderValidator;
    private final OrderShowHideQuestionsCalculator showHideQuestionsCalculator;
    private final ManageOrderDocumentScopedFieldsCalculator fieldsCalculator;
    private final OrderSectionAndQuestionsPrePopulator orderSectionAndQuestionsPrePopulator;
    private final OrderProcessingService orderProcessing;
    private final ManageOrderOperationPostPopulator operationPostPopulator;
    private final ManageOrdersCaseDataFixer manageOrdersCaseDataFixer;
    private final AmendableOrderListBuilder amendableOrderListBuilder;
    private final CoreCaseDataService coreCaseDataService;
    private final ManageOrdersEventBuilder eventBuilder;
    private final ManageOrderPostSubmitHelper postSubmitHelper;
    private static final String PDF = "pdf";

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

        @SuppressWarnings("unchecked") Map<String, Object> manageOrdersAmendedOrder =
            (Map<String, Object>) data.get("manageOrdersAmendedOrder");
        if (manageOrdersAmendedOrder != null) {
            String uploadedFilename = (String) manageOrdersAmendedOrder.get("document_filename");
            if (!PDF.equalsIgnoreCase(getExtension(uploadedFilename))) {
                String userMessage = MessageFormat.format(
                    "Can only amend documents that are {0}, requested document was of type: {1}", PDF,
                    getExtension(uploadedFilename));
                errors.add(userMessage);
            }
        }

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
        CaseDetails updatedDetails = manageOrdersCaseDataFixer.fixAndRetriveCaseDetails(caseDetails);

        Map<String, Object> data = updatedDetails.getData();
        CaseData caseData = fixAndRetrieveCaseData(updatedDetails);

        data.putAll(orderProcessing.process(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails oldCaseDetails = callbackRequest.getCaseDetails();
        ManageOrdersEventData eventData = getCaseData(oldCaseDetails).getManageOrdersEventData();
        // Start event with concurrency controls
        CaseDetails caseDetails = coreCaseDataService.performPostSubmitCallback(oldCaseDetails.getId(),
            "internal-change-manage-order", postSubmitHelper::getPostSubmitUpdates, true);

        if (isEmpty(caseDetails)) {
            // if our callback has failed 3 times, all we have is the prior caseData to send notifications based on
            caseDetails = oldCaseDetails;
        }

        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);
        publishEvent(eventBuilder.build(caseData, caseDataBefore, eventData));
    }

    @PostMapping("/post-submit-callback/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse postHandleAboutToSubmitEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails updatedDetails = manageOrdersCaseDataFixer.fixAndRetriveCaseDetails(caseDetails);
        Map<String, Object> data = updatedDetails.getData();

        fieldsCalculator.calculate().forEach(data::remove);

        return respond(caseDetails);
    }

    private CaseData fixAndRetrieveCaseData(CaseDetails caseDetails) {
        return manageOrdersCaseDataFixer.fix(getCaseData(caseDetails));
    }

}
