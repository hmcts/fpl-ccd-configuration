package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.interfaces.HearingBookingDetailsGroup;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

@Api
@RestController
@RequestMapping("/callback/add-hearing-booking")
public class HearingBookingDetailsController {

    private final MapperService mapperService;
    private final Validator validator;

    @Autowired
    public HearingBookingDetailsController(MapperService mapperService, Validator validator) {
        this.mapperService = mapperService;
        this.validator = validator;
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        CaseData caseData = mapperService.mapObject(caseDetails.getData(), CaseData.class);

        Set<ConstraintViolation<CaseData>> violations = validator.validate(caseData, HearingBookingDetailsGroup.class);

        System.out.println(violations);

        List<String> errors = violations.stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(errors)
            .build();
    }
}
