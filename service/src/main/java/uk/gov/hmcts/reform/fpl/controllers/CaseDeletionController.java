package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.*;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/case-deletion")
public class CaseDeletionController {

    private static final String CONSENT_TEMPLATE = "I, %s, am happy to delete this application.";
    private final UserDetailsService userDetailsService;

    @Autowired
    public CaseDeletionController(
        UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackrequest) {

        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        String label = String.format(CONSENT_TEMPLATE, userDetailsService.getUserName(authorization));

        Map<String, Object> data = caseDetails.getData();
        data.put("deleteConsentLabel", label);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackrequest) {

        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        Map<String, Object> data = caseDetails.getData();
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));

        data.clear();
        data.put("dateDeleted", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }
}
