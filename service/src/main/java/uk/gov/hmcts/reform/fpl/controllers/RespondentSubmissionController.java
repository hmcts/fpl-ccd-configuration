package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Respondents;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/enter-respondents")
@SuppressWarnings("unchecked")
public class RespondentSubmissionController {

    private final MapperService mapperService;
    private final Logger logger = LoggerFactory.getLogger(RespondentSubmissionController.class);

    @Autowired
    public RespondentSubmissionController(MapperService mapperService) {
        this.mapperService = mapperService;
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackrequest) {

        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        List<String> errorsList = new ArrayList<>();

        try {
            Respondents respondents = mapperService.mapObject((Map<String, Object>)
                caseDetails.getData().get("respondents"), Respondents.class);
            if (respondents.getAllRespondents().stream().anyMatch(respondent ->
                respondent.getDob().after(new Date())
            )) {
                errorsList.add("Date of birth cannot be in the future");
            }
        } catch (Exception e) {
            logger.error("exception mapping respondents data " + e.toString());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackrequest.getCaseDetails().getData())
            .errors(Collections.unmodifiableList(errorsList))
            .build();
    }

}
