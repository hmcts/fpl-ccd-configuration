package uk.gov.hmcts.reform.fpl.controllers;


import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.AdditionalRespondent;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.Respondents;
import uk.gov.hmcts.reform.fpl.service.RespondentMapperService;

import java.util.*;

import static uk.gov.hmcts.reform.fpl.validators.DateOfBirthValidator.dateOfBirthIsInFuture;

@Api
@RestController
@RequestMapping("/callback/enter-respondents")
@SuppressWarnings("unchecked")
public class RespondentSubmissionController {

    private final RespondentMapperService respondentMapperService;
    private final String DOB_IN_FUTURE_ERROR_MESSAGE = "Date of birth cannot be in the future";

    @Autowired
    public RespondentSubmissionController(RespondentMapperService respondentMapperService) {
        this.respondentMapperService = respondentMapperService;
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackrequest) {

        boolean addError = false;
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        List<String> errorsList = new ArrayList<>();

        Optional<Respondents> respondents = respondentMapperService.mapRespondents((Map<String, Object>) caseDetails.getData().get("respondents"));
        if (dateOfBirthIsInFuture(respondents.get().getFirstRespondent().getDob())) {
            addError = true;
        } else {
            Iterator<AdditionalRespondent> respondentIterator = respondents.get().getAdditionalRespondents().iterator();
            while (respondentIterator.hasNext()) {
                if (dateOfBirthIsInFuture(respondentIterator.next().getRespondent().getDob())) {
                    addError = true;
                }
            }
        }

        if (addError) {
            errorsList.add(DOB_IN_FUTURE_ERROR_MESSAGE);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackrequest.getCaseDetails().getData())
            .errors(Collections.unmodifiableList(errorsList))
            .build();
    }

}
