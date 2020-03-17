package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.Lists;
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
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.PbaNumberService;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Api
@RestController
@RequestMapping("/callback/upload-c2")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("unchecked")
public class UploadC2DocumentsController {
    private final ObjectMapper mapper;
    private final UserDetailsService userDetailsService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final FeeService feeService;
    private final PaymentService paymentService;
    private final FeatureToggleService featureToggleService;
    private final PbaNumberService pbaNumberService;

    @PostMapping("/get-fee/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        Map<String, Object> data = callbackrequest.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        if (caseData.getTemporaryC2Document() != null && isTemporaryDocumentUrlEmpty(caseData)) {
            ((Map <String, Object>) data.get("temporaryC2Document")).remove("document");
        }

        List<String> errors = new ArrayList<>();
        if (featureToggleService.isFeesEnabled()) {
            try {
                FeesData feesData = feeService.getFeesDataForC2(caseData.getC2ApplicationType().get("type"));
                data.put("amountToPay", BigDecimalHelper.toCCDMoneyGBP(feesData.getTotalAmount()));
            } catch (FeeRegisterException ignore) {
                // TODO: 21/02/2020 Replace me in FPLA-1353
                //  this is an error message for when the Fee Register is unavailable
                errors.add("XXX");
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .errors(errors)
            .build();
    }

    @PostMapping("/validate-pba-number/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleValidatePbaNumberMidEvent(@RequestBody CallbackRequest callbackrequest) {
        Map<String, Object> data = callbackrequest.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        var updatedTemporaryC2Document = pbaNumberService.update(caseData.getTemporaryC2Document());
        data.put("temporaryC2Document", updatedTemporaryC2Document);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .errors(pbaNumberService.validate(updatedTemporaryC2Document))
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackrequest,
        @RequestHeader(value = "authorization") String authorization) {
        Map<String, Object> data = callbackrequest.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.put("c2DocumentBundle", buildC2DocumentBundle(caseData, authorization));
        data.keySet().removeAll(Set.of("temporaryC2Document", "c2ApplicationType", "amountToPay"));

        return AboutToStartOrSubmitCallbackResponse.builder().data(data).build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (featureToggleService.isPaymentsEnabled()) {
            paymentService.makePaymentForC2(caseDetails.getId(), caseData);
        }
        applicationEventPublisher.publishEvent(new C2UploadedEvent(callbackRequest, authorization, userId));
    }

    private boolean isTemporaryDocumentUrlEmpty(CaseData caseData) {
        return Optional.ofNullable(caseData.getTemporaryC2Document())
            .map(C2DocumentBundle::getDocument)
            .map(DocumentReference::getUrl)
            .isEmpty();
    }

    private List<Element<C2DocumentBundle>> buildC2DocumentBundle(CaseData caseData, String authorization) {
        List<Element<C2DocumentBundle>> c2DocumentBundle = defaultIfNull(caseData.getC2DocumentBundle(),
            Lists.newArrayList());
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));

        var c2DocumentBundleBuilder = caseData.getTemporaryC2Document().toBuilder()
            .author(userDetailsService.getUserName(authorization))
            .uploadedDateTime(DateFormatterService.formatLocalDateTimeBaseUsingFormat(zonedDateTime
                .toLocalDateTime(), "h:mma, d MMMM yyyy"));

        if (featureToggleService.isFeesEnabled()) {
            c2DocumentBundleBuilder.type(caseData.getC2ApplicationType().get("type"));
        }

        c2DocumentBundle.add(Element.<C2DocumentBundle>builder()
            .id(UUID.randomUUID())
            .value(c2DocumentBundleBuilder.build())
            .build());

        return c2DocumentBundle;
    }
}
