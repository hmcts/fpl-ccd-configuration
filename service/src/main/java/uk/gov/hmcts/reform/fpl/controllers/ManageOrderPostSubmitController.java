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
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrdersEventBuilder;
import uk.gov.hmcts.reform.fpl.service.orders.OrderProcessingService;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.modifier.ManageOrdersCaseDataFixer;

import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/manage-orders/post-submit-callback")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageOrderPostSubmitController extends CallbackController {
    private final ManageOrderDocumentScopedFieldsCalculator fieldsCalculator;
    private final OrderProcessingService orderProcessing;
    private final ManageOrdersCaseDataFixer manageOrdersCaseDataFixer;
    private final ManageOrdersEventBuilder eventBuilder;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse postHandleAboutToSubmitEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails updatedDetails = manageOrdersCaseDataFixer.fixAndRetriveCaseDetails(caseDetails);
        Map<String, Object> data = updatedDetails.getData();

        fieldsCalculator.calculate().forEach(data::remove);

        return respond(caseDetails);
    }
}
