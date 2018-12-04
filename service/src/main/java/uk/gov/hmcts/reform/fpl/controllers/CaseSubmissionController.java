package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/case-submission")
public class CaseSubmissionController {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserDetailsService userDetailsService;

    @Autowired
    public CaseSubmissionController(ApplicationEventPublisher applicationEventPublisher, UserDetailsService userDetailsService) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/about-to-start")
    public ResponseEntity handleCaseStart(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        String label = "I, " + userDetailsService.getUserName(authorization) + ", believe that the facts stated in this application are true.";

        Map<String, Object> data = caseDetails.getData();
        data.put("submissionConsentLabel", label);

        AboutToStartOrSubmitCallbackResponse body = AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();

        return ResponseEntity.ok(body);
    }

    @PostMapping("/submitted")
    public ResponseEntity handleCaseSubmission(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody @NotNull CallbackRequest callbackRequest) {

        applicationEventPublisher.publishEvent(new SubmittedCaseEvent(callbackRequest, authorization, userId));

        return new ResponseEntity(HttpStatus.OK);
    }
}
