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
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C2_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Api
@RestController
@RequestMapping("/callback/upload-c2")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadC2DocumentsController {
    private static final String TEMPORARY_C2_DOCUMENT = "temporaryC2Document";
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
        data.remove("displayAmountToPay");

        //workaround for previous-continue bug
        if (shouldRemoveDocument(caseData)) {
            removeDocumentFromData(data);
        }

        try {
            if (featureToggleService.isFeesEnabled()) {
                FeesData feesData = feeService.getFeesDataForC2(caseData.getC2ApplicationType().get("type"));
                data.put("amountToPay", BigDecimalHelper.toCCDMoneyGBP(feesData.getTotalAmount()));
                data.put("displayAmountToPay", YES.getValue());
            }
        } catch (FeeRegisterException ignore) {
            data.put("displayAmountToPay", NO.getValue());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/validate-pba-number/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleValidatePbaNumberMidEvent(
        @RequestBody CallbackRequest callbackrequest) {
        Map<String, Object> data = callbackrequest.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        var updatedTemporaryC2Document = pbaNumberService.update(caseData.getTemporaryC2Document());
        data.put(TEMPORARY_C2_DOCUMENT, updatedTemporaryC2Document);

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
        data.keySet().removeAll(Set.of(TEMPORARY_C2_DOCUMENT, "c2ApplicationType", "amountToPay"));

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

            if (displayAmountToPay(caseDetails)) {
                try {
                    paymentService.makePaymentForC2(caseDetails.getId(), caseData);
                } catch (FeeRegisterException | PaymentsApiException ignore) {
                    applicationEventPublisher.publishEvent(new FailedPBAPaymentEvent(callbackRequest,
                        authorization, userId, C2_APPLICATION));
                }
            }

            if (NO.getValue().equals(caseDetails.getData().get("displayAmountToPay"))) {
                applicationEventPublisher.publishEvent(new FailedPBAPaymentEvent(callbackRequest, authorization, userId,
                    C2_APPLICATION));
            }
        }
        applicationEventPublisher.publishEvent(new C2UploadedEvent(callbackRequest, authorization, userId));
    }

    private boolean shouldRemoveDocument(CaseData caseData) {
        return caseData.getTemporaryC2Document() != null
            && caseData.getTemporaryC2Document().getDocument().getUrl() == null;
    }


    private void removeDocumentFromData(Map<String, Object> data) {
        var updatedC2DocumentMap = mapper.convertValue(data.get(TEMPORARY_C2_DOCUMENT), Map.class);
        updatedC2DocumentMap.remove("document");
        data.put(TEMPORARY_C2_DOCUMENT, updatedC2DocumentMap);
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

    private boolean displayAmountToPay(CaseDetails caseDetails) {
        return YES.getValue().equals(caseDetails.getData().get("displayAmountToPay"));
    }
}
