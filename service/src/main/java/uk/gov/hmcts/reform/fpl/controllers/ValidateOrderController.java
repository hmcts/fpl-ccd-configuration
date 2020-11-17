package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.DateOfIssueGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.InterimEndDateGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.epoordergroup.EPOAddressGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.epoordergroup.EPOEndDateGroup;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.PREVENT_REMOVAL;

@Api
@RestController
@RequestMapping("/callback/validate-order")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ValidateOrderController extends CallbackController {
    private final ValidateGroupService validateGroupService;

    @PostMapping("/date-of-issue/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEventValidateDateOfIssue(
        @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        List<String> errors = validateGroupService.validateGroup(caseData, DateOfIssueGroup.class);

        if (GeneratedOrderSubtype.INTERIM.equals(caseData.getOrderTypeAndDocument().getSubtype())) {
            errors.addAll(validateGroupService.validateGroup(
                caseData.getInterimEndDate(), InterimEndDateGroup.class
            ));
        }

        return respond(caseDetails, errors);
    }

    @PostMapping("/address/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEventValidateAddress(
        @RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = List.of();

        if (caseData.getEpoType() == PREVENT_REMOVAL) {
            errors = validateGroupService.validateGroup(caseData, EPOAddressGroup.class);
        }

        return respond(caseDetails, errors);
    }

    @PostMapping("/child-selector/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEventValidateChildren(
        @RequestBody CallbackRequest callbackRequest) {

        CaseData caseData = getCaseData(callbackRequest);

        List<String> errors = new ArrayList<>();

        if (StringUtils.isEmpty(caseData.getRemainingChildIndex())
            && caseData.getChildSelector().getSelected().isEmpty()) {
            errors.add("Select the children included in the order.");
        }

        return respond(callbackRequest.getCaseDetails(), errors);
    }

    @PostMapping("/epo-end-date/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEventValidateEPOEndDate(
        @RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(callbackrequest);

        return respond(caseDetails, validateGroupService.validateGroup(caseData, EPOEndDateGroup.class));
    }

    @PostMapping("/care-orders-selection/mid-event")
    public AboutToStartOrSubmitCallbackResponse validateDischargedOrders(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);
        List<String> errors = new ArrayList<>();

        if (isEmpty(caseData.getCareOrderSelector()) || isEmpty(caseData.getCareOrderSelector().getSelected())) {
            errors.add("Select care orders to be discharged.");
        }

        return respond(callbackRequest.getCaseDetails(), errors);
    }

}
