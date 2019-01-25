package uk.gov.hmcts.reform.fpl.controllers;


import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.AdditionalRespondent;
import uk.gov.hmcts.reform.fpl.model.Respondents;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

@Api
@RestController
@RequestMapping("/callback/enter-respondents")
@SuppressWarnings("unchecked")
public class RespondentSubmissionController {

    private final MapperService mapperService;
    private final String DOB_IN_FUTURE_ERROR_MESSAGE = "Date of birth cannot be in the future";
    private final Logger logger = LoggerFactory.getLogger(RespondentSubmissionController.class);

    @Autowired
    public RespondentSubmissionController(MapperService mapperService) {
        this.mapperService = mapperService;
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackrequest) {

        boolean addError = false;
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        List<String> errorsList = new ArrayList<>();

        try {
            Respondents respondents = mapperService.mapObject((Map<String, Object>) caseDetails.getData().get("respondents"), Respondents.class);
            if (respondents.getFirstRespondent().getDob().after(new Date())) {
                addError = true;
            } else {
                Iterator<AdditionalRespondent> respondentIterator = respondents.getAdditionalRespondents().iterator();
                while (respondentIterator.hasNext()) {
                    if (respondentIterator.next().getRespondent().getDob().after(new Date())) {
                        addError = true;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("exception mapping " + e.toString());
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
