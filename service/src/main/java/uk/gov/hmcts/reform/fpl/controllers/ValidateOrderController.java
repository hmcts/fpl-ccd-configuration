package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.generatedorder.InterimEndDate;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.epoordergroup.EPOAddressGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.epoordergroup.EPOEndDateGroup;

import java.util.List;
import javax.validation.groups.Default;

import static uk.gov.hmcts.reform.fpl.enums.EPOType.PREVENT_REMOVAL;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.NAMED_DATE;

@Api
@RestController
@RequestMapping("/callback/validate-order")
public class ValidateOrderController {
    private final ObjectMapper mapper;
    private final ValidateGroupService validateGroupService;

    @Autowired
    public ValidateOrderController(ObjectMapper mapper, ValidateGroupService validateGroupService) {
        this.mapper = mapper;
        this.validateGroupService = validateGroupService;
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

    @PostMapping("/interim-end-date/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEventValidateInterimEndDate(
        @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        final InterimEndDate interimEndDate = caseData.getInterimEndDate();

        List<String> errors = List.of();

        if (interimEndDate.getType() == NAMED_DATE) {
            errors = validateGroupService.validateGroup(interimEndDate, Default.class);
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
