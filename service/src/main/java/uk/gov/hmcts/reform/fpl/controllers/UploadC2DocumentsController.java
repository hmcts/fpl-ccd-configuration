package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.enums.ApplicantType;
import uk.gov.hmcts.reform.fpl.events.C2PbaPaymentNotTakenEvent;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.service.PbaNumberService;
import uk.gov.hmcts.reform.fpl.service.UploadC2DocumentsService;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C2_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Slf4j
@RestController
@RequestMapping("/callback/upload-c2")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadC2DocumentsController extends CallbackController {
    private static final String DISPLAY_AMOUNT_TO_PAY = "displayAmountToPay";
    private static final String AMOUNT_TO_PAY = "amountToPay";
    private static final String TEMPORARY_C2_DOCUMENT = "temporaryC2Document";
    private final ObjectMapper mapper;
    private final FeeService feeService;
    private final PaymentService paymentService;
    private final PbaNumberService pbaNumberService;
    private final Time time;
    private final UploadC2DocumentsService uploadC2DocumentsService;

    @PostMapping("/get-fee/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);
        data.remove(DISPLAY_AMOUNT_TO_PAY);

        //workaround for previous-continue bug
        if (shouldRemoveDocument(caseData)) {
            removeDocumentFromData(data);
        }

        try {
            FeesData feesData = feeService.getFeesDataForC2(caseData.getC2ApplicationType().get("type"));
            data.put(AMOUNT_TO_PAY, BigDecimalHelper.toCCDMoneyGBP(feesData.getTotalAmount()));
            data.put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());
        } catch (FeeRegisterException ignore) {
            data.put(DISPLAY_AMOUNT_TO_PAY, NO.getValue());
        }

        return respond(caseDetails);
    }

    @PostMapping("/validate/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleValidateMidEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        var updatedTemporaryC2Document = pbaNumberService.update(caseData.getTemporaryC2Document());
        caseDetails.getData().put(TEMPORARY_C2_DOCUMENT, updatedTemporaryC2Document);
        List<String> errors = new ArrayList<>();
        errors.addAll(pbaNumberService.validate(updatedTemporaryC2Document));
        errors.addAll(uploadC2DocumentsService.validate(updatedTemporaryC2Document));

        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put("c2DocumentBundle", uploadC2DocumentsService.buildC2DocumentBundle(caseData));
        caseDetails.getData().keySet().removeAll(Set.of(TEMPORARY_C2_DOCUMENT, "c2ApplicationType", AMOUNT_TO_PAY));

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        final C2DocumentBundle c2DocumentBundle = caseData.getLastC2DocumentBundle();
        publishEvent(new C2UploadedEvent(caseData, c2DocumentBundle));

        if (isNotPaidByPba(c2DocumentBundle)) {
            log.info("C2 payment for case {} not taken due to user decision", caseDetails.getId());
            publishEvent(new C2PbaPaymentNotTakenEvent(caseData));
        } else {
            if (displayAmountToPay(caseDetails)) {
                try {
                    paymentService.makePaymentForC2(caseDetails.getId(), caseData);
                } catch (FeeRegisterException | PaymentsApiException paymentException) {
                    log.error("C2 payment for case {} failed", caseDetails.getId());
                    publishEvent(new FailedPBAPaymentEvent(caseData, List.of(C2_APPLICATION),
                        OrderApplicant.builder().type(ApplicantType.LOCAL_AUTHORITY)
                            .name(caseData.getCaseLocalAuthorityName()).build()));
                }
            } else if (NO.getValue().equals(caseDetails.getData().get(DISPLAY_AMOUNT_TO_PAY))) {
                log.error("C2 payment for case {} not taken as payment fee not shown to user", caseDetails.getId());
                publishEvent(new FailedPBAPaymentEvent(caseData, List.of(C2_APPLICATION),
                    OrderApplicant.builder().type(ApplicantType.LOCAL_AUTHORITY)
                        .name(caseData.getCaseLocalAuthorityName()).build()));
            }
        }
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

    private boolean displayAmountToPay(CaseDetails caseDetails) {
        return YES.getValue().equals(caseDetails.getData().get(DISPLAY_AMOUNT_TO_PAY));
    }

    private boolean isNotPaidByPba(C2DocumentBundle c2DocumentBundle) {
        return NO.getValue().equals(c2DocumentBundle.getUsePbaPayment());
    }
}
