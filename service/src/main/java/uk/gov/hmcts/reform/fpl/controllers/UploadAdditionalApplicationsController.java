package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.service.PbaNumberService;
import uk.gov.hmcts.reform.fpl.service.UploadC2DocumentsService;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ApplicationsFeeCalculator;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;
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
    private final FeeService feeService;
    private final PaymentService paymentService;
    private final PbaNumberService pbaNumberService;
    private final UploadC2DocumentsService uploadC2DocumentsService;
    private final ApplicationsFeeCalculator applicationsFeeCalculator;

    @PostMapping("/get-fee/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(applicationsFeeCalculator.calculateFee(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/validate/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleValidateMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = new ArrayList<>();

        if (ObjectUtils.isEmpty(caseData.getTemporaryPbaPayment())) {
            errors.add("Complete the mandatory fields"); //TODO: check with UX
        } else {
            var updatedPbaNumber = pbaNumberService.update(caseData.getTemporaryPbaPayment().getPbaNumber());
            caseDetails.getData().put("pbaNumber", updatedPbaNumber);
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
        additionalApplications.sort(comparing(e -> e.getValue().getUploadedDateTime(), reverseOrder()));

        C2DocumentBundle c2DocumentBundle = null;
        OtherApplicationsBundle otherApplicationsBundle = null;

        if (caseData.getAdditionalApplicationTypes().contains(AdditionalApplicationType.C2_ORDER)) {
            c2DocumentBundle = uploadC2DocumentsService.buildC2DocumentBundle(caseData).get(0).getValue();
        }

        if (caseData.getAdditionalApplicationTypes().contains(AdditionalApplicationType.OTHER_ORDER)) {
            otherApplicationsBundle = uploadC2DocumentsService.buildOtherApplicationsBundle(caseData);
        }

        additionalApplications.add(0, element(uploadC2DocumentsService.buildAdditionalApplicationsBundle(
            caseData, c2DocumentBundle, otherApplicationsBundle)));

        caseDetails.getData().put("additionalApplicationsBundle", additionalApplications);

        caseDetails.getData().keySet().removeAll(Set.of(TEMPORARY_C2_DOCUMENT,
            "c2ApplicationType",
            "additionalApplicationType",
            "usePbaPayment",
            AMOUNT_TO_PAY,
            "pbaNumber",
            "clientCode",
            "fileReference",
            "temporaryPbaPayment",
            TEMPORARY_OTHER_APPLICATIONS_BUNDLE));

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        final C2DocumentBundle c2DocumentBundle = caseData.getMostRecentC2DocumentBundle();
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
                    publishEvent(new FailedPBAPaymentEvent(caseData, C2_APPLICATION));
                }
            } else if (NO.getValue().equals(caseDetails.getData().get(DISPLAY_AMOUNT_TO_PAY))) {
                log.error("C2 payment for case {} not taken as payment fee not shown to user", caseDetails.getId());
                publishEvent(new FailedPBAPaymentEvent(caseData, C2_APPLICATION));
            }
        }
    }

    private boolean displayAmountToPay(CaseDetails caseDetails) {
        return YES.getValue().equals(caseDetails.getData().get(DISPLAY_AMOUNT_TO_PAY));
    }

    private boolean isNotPaidByPba(C2DocumentBundle c2DocumentBundle) {
        return NO.getValue().equals(c2DocumentBundle.getUsePbaPayment());
    }
}
