package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Respondents;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/enter-respondents")
public class RespondentSubmissionController {

    private final MapperService mapper;

    @Autowired
    public RespondentSubmissionController(MapperService mapper) {
        this.mapper = mapper;
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackrequest.getCaseDetails().getData())
            .errors(validate(caseDetails))
            .build();
    }

    @SuppressWarnings("unchecked")
    private List<String> validate(CaseDetails caseDetails) {
        ImmutableList.Builder<String> errors = ImmutableList.builder();

        Map<String, Object> respondentsData = (Map<String, Object>) caseDetails.getData().get("respondents");
        Respondents respondents = mapper.mapObject(respondentsData, Respondents.class);
        if (respondents.getAllRespondents().stream().anyMatch(respondent -> respondent.getDob().after(new Date()))) {
            errors.add("Date of birth cannot be in the future");
        }

        return errors.build();
    }

}
