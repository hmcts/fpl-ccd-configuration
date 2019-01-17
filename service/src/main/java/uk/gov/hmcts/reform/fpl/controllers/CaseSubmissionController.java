package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.tuple.Pair;
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
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.DocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import java.util.Map;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.reform.fpl.utils.SubmittedFormFilenameHelper.buildFileName;

@Api
@RestController
@RequestMapping("/callback/case-submission")
public class CaseSubmissionController {

    private static final String CONSENT_TEMPLATE = "I, %s, believe that the facts stated in this application are true.";
    private final UserDetailsService userDetailsService;
    private final DocumentGeneratorService documentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public CaseSubmissionController(
        UserDetailsService userDetailsService,
        DocumentGeneratorService documentGeneratorService,
        UploadDocumentService uploadDocumentService,
        ApplicationEventPublisher applicationEventPublisher) {
        this.userDetailsService = userDetailsService;
        this.documentGeneratorService = documentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        String label = String.format(CONSENT_TEMPLATE, userDetailsService.getUserName(authorization));

        Map<String, Object> data = caseDetails.getData();
        data.put("submissionConsentLabel", label);

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

        byte[] pdf = documentGeneratorService.generateSubmittedFormPDF(caseDetails,
            Pair.of("userFullName", userDetailsService.getUserName(authorization))
        );

        Document document = uploadDocumentService.uploadPDF(userId, authorization, pdf, buildFileName(caseDetails));

        Map<String, Object> data = caseDetails.getData();
        data.put("submittedForm", ImmutableMap.<String, String>builder()
            .put("document_url", document.links.self.href)
            .put("document_binary_url", document.links.binary.href)
            .put("document_filename", document.originalDocumentName)
            .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody @NotNull CallbackRequest callbackRequest) {

        applicationEventPublisher.publishEvent(new SubmittedCaseEvent(callbackRequest, authorization, userId));
    }
}
