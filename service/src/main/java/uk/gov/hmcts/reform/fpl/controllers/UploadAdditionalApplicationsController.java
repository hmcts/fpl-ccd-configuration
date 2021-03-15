package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
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
import uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType;
import uk.gov.hmcts.reform.fpl.events.C2PbaPaymentNotTakenEvent;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.service.PbaNumberService;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ApplicationsFeeCalculator;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.UploadAdditionalApplicationsService;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C2_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Api
@Slf4j
@RestController
@RequestMapping("/callback/upload-additional-applications")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadAdditionalApplicationsController extends CallbackController {
    private static final String DISPLAY_AMOUNT_TO_PAY = "displayAmountToPay";
    private static final String AMOUNT_TO_PAY = "amountToPay";
    private static final String TEMPORARY_C2_DOCUMENT = "temporaryC2Document";
    private static final String TEMPORARY_OTHER_APPLICATIONS_BUNDLE = "temporaryOtherApplicationsBundle";
    private final PaymentService paymentService;
    private final PbaNumberService pbaNumberService;
    private final UploadAdditionalApplicationsService uploadC2DocumentsService;
    private final ApplicationsFeeCalculator applicationsFeeCalculator;

    @PostMapping("/get-fee/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(applicationsFeeCalculator.calculateFee(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/validate/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleValidateMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = new ArrayList<>();

        if (caseData.getTemporaryPbaPayment() != null) {
            var updatedPbaNumber = pbaNumberService.update(caseData.getTemporaryPbaPayment().getPbaNumber());
            caseDetails.getData().put("temporaryPbaPayment",
                caseData.getTemporaryPbaPayment().toBuilder().pbaNumber(updatedPbaNumber).build());
            errors.addAll(pbaNumberService.validate(updatedPbaNumber));
        }

        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<Element<AdditionalApplicationsBundle>> additionalApplications = defaultIfNull(
            caseData.getAdditionalApplicationsBundle(), new ArrayList<>()
        );

        C2DocumentBundle c2DocumentBundle = null;
        OtherApplicationsBundle otherApplicationsBundle = null;

        if (hasC2Order(caseData)) {
            c2DocumentBundle = uploadC2DocumentsService.buildC2DocumentBundle(caseData);
        }

        if (hasOtherApplicationsOrder(caseData)) {
            otherApplicationsBundle = uploadC2DocumentsService.buildOtherApplicationsBundle(caseData);
        }

        additionalApplications.add(0, element(uploadC2DocumentsService.buildAdditionalApplicationsBundle(
            caseData, c2DocumentBundle, otherApplicationsBundle)));

        caseDetails.getData().put("additionalApplicationsBundle", additionalApplications);

        removeTemporaryFields(caseDetails);

        return respond(caseDetails);
    }

    private void removeTemporaryFields(CaseDetails caseDetails) {
        caseDetails.getData().keySet().removeAll(Set.of(TEMPORARY_C2_DOCUMENT,
            "c2Type",
            "additionalApplicationType",
            "usePbaPayment",
            AMOUNT_TO_PAY,
            "pbaNumber",
            "clientCode",
            "fileReference",
            "temporaryPbaPayment",
            TEMPORARY_OTHER_APPLICATIONS_BUNDLE));
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        final PBAPayment pbaPayment = caseData.getAdditionalApplicationsBundle().get(0).getValue().getPbaPayment();

        if (hasC2Order(caseData)) {
            C2DocumentBundle c2DocumentBundle = caseData.getAdditionalApplicationsBundle()
                .get(0).getValue().getC2DocumentBundle();

            if (pbaPayment != null) {
                c2DocumentBundle.toBuilder()
                    .usePbaPayment(pbaPayment.getUsePbaPayment())
                    .pbaNumber(pbaPayment.getPbaNumber())
                    .clientCode(pbaPayment.getClientCode())
                    .fileReference(pbaPayment.getFileReference()).build();
            }

            publishEvent(new C2UploadedEvent(caseData, c2DocumentBundle));

            if (isNotPaidByPba(c2DocumentBundle)) {
                log.info("Payment for case {} not taken due to user decision", caseDetails.getId());
                publishEvent(new C2PbaPaymentNotTakenEvent(caseData));
            } else {
                if (displayAmountToPay(caseDetails)) {
                    try {
                        paymentService.makePaymentForC2(caseDetails.getId(), caseData);
                    } catch (FeeRegisterException | PaymentsApiException paymentException) {
                        log.error("C2 payment for case {} failed", caseDetails.getId());
                        publishEvent(new FailedPBAPaymentEvent(caseData, C2_APPLICATION));
                    }
                } else if (NO.getValue().equals(caseDetails.getData().get(DISPLAY_AMOUNT_TO_PAY))) {
                    log.error("C2 payment for case {} not taken as payment fee not shown to user", caseDetails.getId());
                    publishEvent(new FailedPBAPaymentEvent(caseData, C2_APPLICATION));
                }
            }
        }
    }

    private boolean hasC2Order(CaseData caseData) {
        return caseData.getAdditionalApplicationType().contains(AdditionalApplicationType.C2_ORDER);
    }

    private boolean hasOtherApplicationsOrder(CaseData caseData) {
        return caseData.getAdditionalApplicationType().contains(AdditionalApplicationType.OTHER_ORDER);
    }

    private boolean displayAmountToPay(CaseDetails caseDetails) {
        return YES.getValue().equals(caseDetails.getData().get(DISPLAY_AMOUNT_TO_PAY));
    }

    private boolean isNotPaidByPba(C2DocumentBundle c2DocumentBundle) {
        return NO.getValue().equals(c2DocumentBundle.getUsePbaPayment());
    }
}
