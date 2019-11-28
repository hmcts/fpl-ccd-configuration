package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.fpl.config.GatewayConfiguration;
import uk.gov.hmcts.reform.fpl.events.CMOIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Api
@RestController
@RequestMapping("/callback/action-cmo")
public class ActionCMOController {

    private final ObjectMapper mapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final GatewayConfiguration gatewayConfiguration;

    @Autowired
    public ActionCMOController(ObjectMapper mapper,
                              ApplicationEventPublisher applicationEventPublisher,
                              GatewayConfiguration gatewayConfiguration) {
        this.mapper = mapper;
        this.applicationEventPublisher = applicationEventPublisher;
        this.gatewayConfiguration = gatewayConfiguration;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestHeader(value = "authorization") String authorization,
                                     @RequestHeader(value = "user-id") String userId,
                                     @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        String documentUrl = caseData.getCaseManagementOrder().getOrderDoc().getBinaryUrl();

        applicationEventPublisher.publishEvent(new CMOIssuedEvent(callbackRequest, authorization, userId,
            concatGatewayConfigurationUrlAndUploadedDocumentPath(documentUrl)));
    }

    private String concatGatewayConfigurationUrlAndUploadedDocumentPath(
        final String documentUrl) {
        final String gatewayUrl = gatewayConfiguration.getUrl();

        try {
            URI uri = new URI(documentUrl);
            return gatewayUrl + uri.getPath();
        } catch (URISyntaxException e) {
            log.error(documentUrl + " url incorrect.", e);
        }
        return "";
    }
}
