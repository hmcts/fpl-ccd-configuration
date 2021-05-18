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
import uk.gov.hmcts.reform.fpl.events.AdditionalApplicationsPbaPaymentNotTakenEvent;
import uk.gov.hmcts.reform.fpl.events.AdditionalApplicationsUploadedEvent;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.PbaNumberService;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ApplicationsFeeCalculator;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.UploadAdditionalApplicationsService;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C2_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
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
    private final UploadAdditionalApplicationsService uploadAdditionalApplicationsService;
    private final ApplicationsFeeCalculator applicationsFeeCalculator;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put("temporaryApplicantsList",
            uploadAdditionalApplicationsService.buildApplicantsList(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/get-fee/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        if (!isNull(caseData.getTemporaryC2Document())) {
            caseData.getTemporaryC2Document().setType(caseData.getC2Type());
            caseDetails.getData().put("temporaryC2Document", caseData.getTemporaryC2Document());
        }

        caseDetails.getData().putAll(applicationsFeeCalculator.calculateFee(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/validate/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleValidateMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        PBAPayment updatedPbaPayment = pbaNumberService.updatePBAPayment(caseData.getTemporaryPbaPayment());
        caseDetails.getData().put("temporaryPbaPayment", updatedPbaPayment);

        return respond(caseDetails, pbaNumberService.validate(updatedPbaPayment));
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<Element<AdditionalApplicationsBundle>> additionalApplications = defaultIfNull(
            caseData.getAdditionalApplicationsBundle(), new ArrayList<>()
        );

        additionalApplications.add(0, element(
            uploadAdditionalApplicationsService.buildAdditionalApplicationsBundle(caseData)));

        caseDetails.getData().put("additionalApplicationsBundle", additionalApplications);

        List<Element<C2DocumentBundle>> oldC2DocumentCollection = defaultIfNull(
            caseData.getC2DocumentBundle(), new ArrayList<>()
        );

        caseDetails.getData().put("c2DocumentBundle", uploadAdditionalApplicationsService
            .sortOldC2DocumentCollection(oldC2DocumentCollection));

        removeTemporaryFields(caseDetails, TEMPORARY_C2_DOCUMENT, "c2Type", "additionalApplicationType",
            AMOUNT_TO_PAY, "temporaryPbaPayment", TEMPORARY_OTHER_APPLICATIONS_BUNDLE);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        final AdditionalApplicationsBundle lastBundle = caseData.getAdditionalApplicationsBundle().get(0).getValue();

        final PBAPayment pbaPayment = lastBundle.getPbaPayment();

        publishEvent(new AdditionalApplicationsUploadedEvent(caseData));

        if (isNotPaidByPba(pbaPayment)) {
            log.info("Payment for case {} not taken due to user decision", caseDetails.getId());
            publishEvent(new AdditionalApplicationsPbaPaymentNotTakenEvent(caseData));
        } else {
            if (amountToPayShownToUser(caseDetails)) {
                try {
                    FeesData feesData = applicationsFeeCalculator.getFeeDataForAdditionalApplications(lastBundle);
                    paymentService.makePaymentForAdditionalApplications(caseDetails.getId(), caseData, feesData);
                } catch (FeeRegisterException | PaymentsApiException paymentException) {
                    log.error("Additional applications payment for case {} failed", caseDetails.getId());
                    publishEvent(new FailedPBAPaymentEvent(caseData, C2_APPLICATION));
                }
            } else if (NO.getValue().equals(caseDetails.getData().get(DISPLAY_AMOUNT_TO_PAY))) {
                log.error("Additional applications payment for case {} not taken as payment fee not shown to user",
                    caseDetails.getId());
                publishEvent(new FailedPBAPaymentEvent(caseData, C2_APPLICATION));
            }
        }
    }

    private boolean amountToPayShownToUser(CaseDetails caseDetails) {
        return YES.getValue().equals(caseDetails.getData().get(DISPLAY_AMOUNT_TO_PAY));
    }

    private boolean isNotPaidByPba(PBAPayment pbaPayment) {
        return pbaPayment != null && NO.getValue().equals(pbaPayment.getUsePbaPayment());
    }
}
