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
import uk.gov.hmcts.reform.fpl.events.InitiatedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityNameService;

import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/case-initiation")
public class CaseInitiationController {

    private final LocalAuthorityNameService localAuthorityNameService;
    private final ApplicationEventPublisher applicationEventPublisher;


    @Autowired
    public CaseInitiationController(LocalAuthorityNameService localAuthorityNameService,
                                    ApplicationEventPublisher applicationEventPublisher) {
        this.localAuthorityNameService = localAuthorityNameService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostMapping("/about-to-submit")
    public ResponseEntity createdCase(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackrequest) {
        String caseLocalAuthority = localAuthorityNameService.getLocalAuthorityCode(authorization);
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        Map<String, Object> data = caseDetails.getData();
        data.put("caseLocalAuthority", caseLocalAuthority);

        AboutToStartOrSubmitCallbackResponse body = AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();

        return ResponseEntity.ok(body);
    }

    @PostMapping("/submitted")
    public ResponseEntity handleCaseInitiation(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) {

        applicationEventPublisher.publishEvent(new InitiatedCaseEvent(callbackRequest, authorization, userId));

        return new ResponseEntity(HttpStatus.OK);
    }
}
