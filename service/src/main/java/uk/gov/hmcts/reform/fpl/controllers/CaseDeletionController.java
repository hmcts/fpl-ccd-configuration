package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.DeletedCaseEvent;

import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.validation.constraints.NotNull;

@Api
@RestController
@RequestMapping("/callback/case-deletion")
public class CaseDeletionController {

    private static final String CONSENT_TEMPLATE = "I, %s, am happy to delete this application.";
    private final UserDetailsService userDetailsService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public CaseDeletionController(
        UserDetailsService userDetailsService,
        ApplicationEventPublisher applicationEventPublisher) {
        this.userDetailsService = userDetailsService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackrequest) {

        System.out.println("******** START: CASE DELETION: ABOUT TO START ***********");
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        String label = String.format(CONSENT_TEMPLATE, userDetailsService.getUserName(authorization));

        Map<String, Object> data = caseDetails.getData();
        data.put("deletionConsentLabel", label);

        System.out.println("******** END: CASE DELETION: ABOUT TO START ***********");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackrequest) {

        System.out.println("******** START: CASE DELETION: ABOUT TO SUBMIT ***********");
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        Map<String, Object> data = caseDetails.getData();
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        data.put("dateDeleted", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime));

        System.out.println("******** END: CASE DELETION: ABOUT TO SUBMIT ***********");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody @NotNull CallbackRequest callbackRequest) {

        System.out.println("******** START: CASE DELETION: SUBMITTED ***********");
        applicationEventPublisher.publishEvent(new DeletedCaseEvent(callbackRequest, authorization, userId));
        System.out.println("******** END: CASE DELETION: SUBMITTED ***********");
    }
}
