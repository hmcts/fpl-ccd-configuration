package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.RestrictionsConfiguration;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CaseValidatorService;
import uk.gov.hmcts.reform.fpl.service.CaseSubmissionService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper;
import uk.gov.hmcts.reform.fpl.validation.groups.EPOGroup;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.SubmittedFormFilenameHelper.buildFileName;

@Api
@RestController
@RequestMapping("/callback/case-submission")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseSubmissionController {
    private static final String DISPLAY_AMOUNT_TO_PAY = "displayAmountToPay";
    private static final String CONSENT_TEMPLATE = "I, %s, believe that the facts stated in this application are true.";
    private final UserDetailsService userDetailsService;
    private final CaseSubmissionService caseSubmissionService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CaseValidatorService caseValidatorService;
    private final ObjectMapper mapper;
    private final RestrictionsConfiguration restrictionsConfiguration;
    private final PaymentService paymentService;
    private final FeeService feeService;
    private final FeatureToggleService featureToggleService;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final RequestData requestData;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(
        @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.remove(DISPLAY_AMOUNT_TO_PAY);
        List<String> errors = validate(caseData);

        if (errors.isEmpty()) {
            try {
                if (featureToggleService.isFeesEnabled()) {
                    FeesData feesData = feeService.getFeesDataForOrders(caseData.getOrders());
                    data.put("amountToPay", BigDecimalHelper.toCCDMoneyGBP(feesData.getTotalAmount()));
                    data.put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());
                }
            } catch (FeeRegisterException ignore) {
                data.put(DISPLAY_AMOUNT_TO_PAY, NO.getValue());
            }
            String label = String.format(CONSENT_TEMPLATE, userDetailsService.getUserName());
            data.put("submissionConsentLabel", label);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .errors(errors)
            .build();
    }

    private List<String> validate(CaseData caseData) {
        List<String> errors = new ArrayList<>();

        if (restrictionsConfiguration.getLocalAuthorityCodesForbiddenCaseSubmission()
            .contains(caseData.getCaseLocalAuthority())) {
            errors.add("Test local authority cannot submit cases");
        }

        return errors;
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseData != null && caseData.getOrders() != null && caseData.getOrders().getOrderType() != null
            && caseData.getOrders().getOrderType().contains(OrderType.EMERGENCY_PROTECTION_ORDER)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData())
                .errors(caseValidatorService.validateCaseDetails(caseData, Default.class, EPOGroup.class))
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(caseValidatorService.validateCaseDetails(caseData))
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(@RequestBody CallbackRequest callbackRequest)
            throws IOException {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        Document document = caseSubmissionService.generateSubmittedFormPDF(caseData, buildFileName(caseDetails));

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));

        Map<String, Object> data = caseDetails.getData();
        data.put("dateAndTimeSubmitted", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime));
        data.put("dateSubmitted", DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime));
        data.put("sendToCtsc", setSendToCtsc(data.get("caseLocalAuthority").toString()));
        data.put("submittedForm", ImmutableMap.<String, String>builder()
            .put("document_url", document.links.self.href)
            .put("document_binary_url", document.links.binary.href)
            .put("document_filename", document.originalDocumentName)
            .build());
        data.remove("amountToPay");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(
        @RequestBody @NotNull CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        if (featureToggleService.isPaymentsEnabled()) {

            if (displayAmountToPay(caseDetails)) {
                try {
                    paymentService.makePaymentForCaseOrders(caseDetails.getId(), caseData);
                } catch (FeeRegisterException | PaymentsApiException ignore) {
                    applicationEventPublisher.publishEvent(new FailedPBAPaymentEvent(callbackRequest, requestData,
                        C110A_APPLICATION));
                }
            }

            if (NO.getValue().equals(caseDetails.getData().get(DISPLAY_AMOUNT_TO_PAY))) {
                applicationEventPublisher.publishEvent(new FailedPBAPaymentEvent(callbackRequest, requestData,
                    C110A_APPLICATION));
            }
        }
        applicationEventPublisher.publishEvent(new SubmittedCaseEvent(callbackRequest, requestData));
    }

    private String setSendToCtsc(String caseLocalAuthority) {
        String localAuthorityName = localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseLocalAuthority);

        return featureToggleService.isCtscEnabled(localAuthorityName) ? YES.getValue() : NO.getValue();
    }

    private boolean displayAmountToPay(CaseDetails caseDetails) {
        return YES.getValue().equals(caseDetails.getData().get(DISPLAY_AMOUNT_TO_PAY));
    }
}
