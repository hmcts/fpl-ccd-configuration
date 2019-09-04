package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseValidatorService;
import uk.gov.hmcts.reform.fpl.service.DocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import javax.validation.constraints.NotNull;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.SubmittedFormFilenameHelper.buildFileName;

@Api
@RestController
@RequestMapping("/callback/allocation-decision")
public class AllocationDecisionController {

    private static final String CONSENT_TEMPLATE = "I, %s, believe that the facts stated in this application are true.";
    private final UserDetailsService userDetailsService;
    private final DocumentGeneratorService documentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CaseValidatorService caseValidatorService;
    private final ObjectMapper mapper;

    @Autowired
    public AllocationDecisionController(
        UserDetailsService userDetailsService,
        DocumentGeneratorService documentGeneratorService,
        UploadDocumentService uploadDocumentService,
        CaseValidatorService caseValidatorService,
        ObjectMapper mapper,
        ApplicationEventPublisher applicationEventPublisher) {
        this.userDetailsService = userDetailsService;
        this.documentGeneratorService = documentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.caseValidatorService = caseValidatorService;
        this.mapper = mapper;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        String label = String.format(CONSENT_TEMPLATE, userDetailsService.getUserName(authorization));

        Map<String, Object> data = caseDetails.getData();
        data.put("submissionConsentLabel", label);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/for-proposal")
    public AboutToStartOrSubmitCallbackResponse checkIfAllocationProposalIsMissing(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        Map<String, Object> data = caseDetails.getData();

        data.put("missingProposal", checkProposal(data));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private String checkProposal(Map<String, Object> data) {
        return data.containsKey("allocationProposal") ? "" : "Yes";
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseData != null && caseData.getOrders() != null && caseData.getOrders().getOrderType() != null
            && caseData.getOrders().getOrderType().contains(OrderType.EMERGENCY_PROTECTION_ORDER)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData())
                //HACK CAN'T BE MERGED
//                .errors(caseValidatorService.validateCaseDetails(caseData, Default.class, EPOGroup.class))
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            // HACK CAN'T BE MERGED
//            .errors(caseValidatorService.validateCaseDetails(caseData))
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        byte[] pdf = documentGeneratorService.generateSubmittedFormPDF(caseDetails,
            Pair.of("userFullName", userDetailsService.getUserName(authorization))
        );

        Document document = uploadDocumentService.uploadPDF(userId, authorization, pdf, buildFileName(caseDetails));

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));

        Map<String, Object> data = caseDetails.getData();
        data.put("dateAndTimeSubmitted", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime));
        data.put("dateSubmitted", DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime));
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
