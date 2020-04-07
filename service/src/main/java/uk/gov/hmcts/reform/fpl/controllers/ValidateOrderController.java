package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.DateOfIssueGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.InterimEndDateGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.epoordergroup.EPOAddressGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.epoordergroup.EPOEndDateGroup;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.EPOType.PREVENT_REMOVAL;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.NAMED_DATE;

@Api
@RestController
@RequestMapping("/callback/validate-order")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ValidateOrderController {
    private final ObjectMapper mapper;
    private final ValidateGroupService validateGroupService;

    @PostMapping("/date-of-issue/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEventValidateDateOfIssue(
        @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validateGroupService.validateGroup(caseData, DateOfIssueGroup.class))
            .build();
    }

    @PostMapping("/address/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEventValidateAddress(
        @RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<String> errors = List.of();

        if (caseData.getEpoType() == PREVENT_REMOVAL) {
            errors = validateGroupService.validateGroup(caseData, EPOAddressGroup.class);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(errors)
            .build();
    }

    @PostMapping("/child-selector/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEventValidateChildren(
        @RequestBody CallbackRequest callbackRequest) {

        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<String> errors = new ArrayList<>();

        if (caseData.getChildSelector().getSelected().isEmpty()) {
            errors.add("Select the children included in the order.");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(errors)
            .build();
    }

    @PostMapping("/interim-end-date/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEventValidateInterimEndDate(
        @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        final InterimEndDate interimEndDate = caseData.getInterimEndDate();

        List<String> errors = List.of();

        if (interimEndDate.getType() == NAMED_DATE) {
            errors = validateGroupService.validateGroup(interimEndDate, InterimEndDateGroup.class);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(errors)
            .build();
    }

    @PostMapping("/epo-end-date/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEventValidateEPOEndDate(
        @RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validateGroupService.validateGroup(caseData, EPOEndDateGroup.class))
            .build();
    }
}
