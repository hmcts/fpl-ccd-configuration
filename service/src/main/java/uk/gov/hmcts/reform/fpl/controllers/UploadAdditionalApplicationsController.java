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
import uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType;
import uk.gov.hmcts.reform.fpl.events.AdditionalApplicationsPbaPaymentNotTakenEvent;
import uk.gov.hmcts.reform.fpl.events.AdditionalApplicationsUploadedEvent;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.events.order.AdditonalAppLicationDraftOrderUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.DraftOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundles;
import uk.gov.hmcts.reform.fpl.service.PbaNumberService;
import uk.gov.hmcts.reform.fpl.service.PeopleInCaseService;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ApplicantsListGenerator;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ApplicationsFeeCalculator;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.UploadAdditionalApplicationsService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.cmo.DraftOrderService;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested.REQUESTING_ADJOURNMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;

@Slf4j
@RestController
@RequestMapping("/callback/upload-additional-applications")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadAdditionalApplicationsController extends CallbackController {

    private static final String DISPLAY_AMOUNT_TO_PAY = "displayAmountToPay";
    private static final String AMOUNT_TO_PAY = "amountToPay";
    private static final String TEMPORARY_C2_DOCUMENT = "temporaryC2Document";
    private static final String TEMPORARY_OTHER_APPLICATIONS_BUNDLE = "temporaryOtherApplicationsBundle";
    private static final String SKIP_PAYMENT_PAGE = "skipPaymentPage";
    private static final String IS_C2_CONFIDENTIAL = "isC2Confidential";

    private final ObjectMapper mapper;
    private final DraftOrderService draftOrderService;
    private final PaymentService paymentService;
    private final PbaNumberService pbaNumberService;
    private final UploadAdditionalApplicationsService uploadAdditionalApplicationsService;
    private final ApplicationsFeeCalculator applicationsFeeCalculator;
    private final ApplicantsListGenerator applicantsListGenerator;
    private final PeopleInCaseService peopleInCaseService;
    private final CoreCaseDataService coreCaseDataService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put("applicantsList", applicantsListGenerator.buildApplicantsList(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/initial-choice/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleInitialChoiceMidEvent(
        @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        if (caseData.getAdditionalApplicationType().contains(AdditionalApplicationType.C2_ORDER)) {
            // Initialise the C2 document bundle so we can have a dynamic list present
            caseDetails.getData().put(TEMPORARY_C2_DOCUMENT, C2DocumentBundle.builder()
                .hearingList(caseData.buildDynamicHearingList())
                .build());
        }
        return respond(caseDetails);
    }

    @PostMapping({"/get-fee/mid-event", "/populate-data/mid-event"})
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        boolean skipPayment = false;
        if (!isNull(caseData.getTemporaryC2Document())) {
            C2DocumentBundle temporaryC2Document = caseData.getTemporaryC2Document();
            temporaryC2Document.setType(caseData.getC2Type());

            if (!isNull(temporaryC2Document.getC2AdditionalOrdersRequested())
                && temporaryC2Document.getC2AdditionalOrdersRequested().contains(REQUESTING_ADJOURNMENT)) {
                // Get the selected hearing from the dynamic list + populate the 'selected hearing' field
                UUID selectedHearingCode = getDynamicListSelectedValue(temporaryC2Document.getHearingList(), mapper);
                HearingBooking hearing = findElement(selectedHearingCode,
                    caseData.getHearingDetails()).orElseGet(() -> element(HearingBooking.builder().build())).getValue();

                temporaryC2Document = temporaryC2Document.toBuilder()
                    .hearingList(null)
                    .requestedHearingToAdjourn(hearing.toLabel())
                    .build();

                skipPayment = uploadAdditionalApplicationsService.shouldSkipPayments(caseData, hearing,
                    temporaryC2Document);
            }
            caseDetails.getData().put(TEMPORARY_C2_DOCUMENT, temporaryC2Document);
        }

        if (!skipPayment) {
            caseDetails.getData().putAll(applicationsFeeCalculator.calculateFee(caseData));
            caseDetails.getData().put(SKIP_PAYMENT_PAGE, NO.getValue());
        } else {
            caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, NO.getValue());
            caseDetails.getData().put(SKIP_PAYMENT_PAGE, YES.getValue());
        }

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
    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        /* This is a workaround as the 'hearingList' has set itself to the UUID of the selected hearing, NOT the
         * actual DynamicList data structure anymore so cannot deserialize properly (so we just throw it out, as we
         * don't need it at this stage anyway). */
        if (isNotEmpty(caseDetails.getData().get(TEMPORARY_C2_DOCUMENT))) {
            ((LinkedHashMap) caseDetails.getData().get(TEMPORARY_C2_DOCUMENT)).put("hearingList", null);
        }
        CaseData caseData = getCaseData(caseDetails);

        if (!isNull(caseData.getTemporaryC2Document())
            && !caseData.getTemporaryC2Document().getDraftOrdersBundle().isEmpty()) {

            List<Element<DraftOrder>> draftOrders = caseData.getTemporaryC2Document().getDraftOrdersBundle();
            List<Element<HearingOrder>> newDrafts = draftOrders.stream()
                .map(Element::getValue)
                .map(HearingOrder::from)
                .map(ElementUtils::element)
                .collect(Collectors.toList());

            HearingOrdersBundles hearingOrdersBundles = draftOrderService.migrateCmoDraftToOrdersBundles(caseData);

            if (YES.equals(caseData.getIsC2Confidential())) {
                draftOrderService.confidentialAdditionalApplicationUpdateCase(caseData, newDrafts,
                    hearingOrdersBundles.getAgreedCmos());
            } else {
                draftOrderService.additionalApplicationUpdateCase(newDrafts, hearingOrdersBundles.getAgreedCmos());
            }

            caseDetails.getData().put("hearingOrdersBundlesDrafts", hearingOrdersBundles.getAgreedCmos());
        }

        List<Element<AdditionalApplicationsBundle>> additionalApplications = defaultIfNull(
            caseData.getAdditionalApplicationsBundle(), new ArrayList<>()
        );

        AdditionalApplicationsBundle additionalApplicationsBundle =
            uploadAdditionalApplicationsService.buildAdditionalApplicationsBundle(caseData);
        additionalApplications.add(0, element(additionalApplicationsBundle));

        caseDetails.getData().put("additionalApplicationsBundle", additionalApplications);

        List<Element<C2DocumentBundle>> oldC2DocumentCollection = defaultIfNull(
            caseData.getC2DocumentBundle(), new ArrayList<>()
        );

        caseDetails.getData().put("c2DocumentBundle", uploadAdditionalApplicationsService
            .sortOldC2DocumentCollection(oldC2DocumentCollection));

        removeTemporaryFields(caseDetails, TEMPORARY_C2_DOCUMENT, "c2Type",
            "additionalApplicationType", AMOUNT_TO_PAY, "temporaryPbaPayment",
            TEMPORARY_OTHER_APPLICATIONS_BUNDLE, "applicantsList", "otherApplicant", SKIP_PAYMENT_PAGE,
            IS_C2_CONFIDENTIAL);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails oldCaseDetails = callbackRequest.getCaseDetails();
        CaseData oldCaseData = getCaseData(oldCaseDetails);
        final CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        final UUID lastBundleId = oldCaseData.getAdditionalApplicationsBundle().get(0).getId();

        CaseDetails caseDetails = coreCaseDataService.performPostSubmitCallback(oldCaseDetails.getId(),
            "internal-change-upload-add-apps",
            caseDetailsCurrent -> {
                CaseData caseDataCurrent = getCaseData(caseDetailsCurrent);
                final AdditionalApplicationsBundle lastBundle = ElementUtils.findElement(lastBundleId,
                    caseDataCurrent.getAdditionalApplicationsBundle()).map(Element::getValue)
                    .orElseGet(() -> AdditionalApplicationsBundle.builder().build());
                AdditionalApplicationsBundle.AdditionalApplicationsBundleBuilder bundleBuilder = lastBundle.toBuilder();

                // If we have a C2 application, do the conversion if needed
                if (!isEmpty(lastBundle.getC2DocumentBundle())) {
                    bundleBuilder.c2DocumentBundle(
                        uploadAdditionalApplicationsService.convertC2Bundle(lastBundle.getC2DocumentBundle(),
                            caseDataCurrent)
                    );
                }
                if (!isEmpty(lastBundle.getC2DocumentBundleConfidential())) {
                    uploadAdditionalApplicationsService.convertConfidentialC2Bundle(caseDataCurrent,
                        lastBundle.getC2DocumentBundleConfidential(), bundleBuilder);
                }

                // If we have a other application, do conversion if needed
                if (!isEmpty(lastBundle.getOtherApplicationsBundle())) {
                    bundleBuilder.otherApplicationsBundle(
                        uploadAdditionalApplicationsService.convertOtherBundle(lastBundle.getOtherApplicationsBundle(),
                            caseDataCurrent)
                    );
                }

                // update with our newly converted bundles (may not have changed at all, but we can't tell easily as it
                // could be a supplement
                List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle
                    = caseDataCurrent.getAdditionalApplicationsBundle().stream()
                    .filter(bundle -> !bundle.getId().equals(lastBundleId))
                    .collect(Collectors.toList());

                additionalApplicationsBundle.add(0, element(lastBundleId, bundleBuilder.build()));

                return Map.of(
                    "additionalApplicationsBundle", additionalApplicationsBundle
                );
            });

        if (isEmpty(caseDetails)) {
            // if our callback has failed 3 times, all we have is the prior caseData to send notifications based on
            caseDetails = oldCaseDetails;
        }

        CaseData caseData = getCaseData(caseDetails);
        final AdditionalApplicationsBundle lastBundle = ElementUtils.findElement(lastBundleId,
                caseData.getAdditionalApplicationsBundle()).map(Element::getValue)
            .orElseGet(() -> AdditionalApplicationsBundle.builder().build());

        final PBAPayment pbaPayment = lastBundle.getPbaPayment();

        publishEvent(new AdditionalApplicationsUploadedEvent(caseData, caseDataBefore,
            applicantsListGenerator.getApplicant(caseData, lastBundle)));

        publishEvent(new AdditonalAppLicationDraftOrderUploadedEvent(caseData, caseDataBefore));

        if (isNotPaidByPba(pbaPayment)) {
            log.info("Payment for case {} not taken due to user decision", caseDetails.getId());
            publishEvent(new AdditionalApplicationsPbaPaymentNotTakenEvent(caseData));
        } else {
            if (isNotEmpty(lastBundle.getC2DocumentBundle())
                && isNotEmpty(lastBundle.getC2DocumentBundle().getRequestedHearingToAdjourn())
                && uploadAdditionalApplicationsService.onlyApplyingForAnAdjournment(caseData,
                    lastBundle.getC2DocumentBundle())) {
                // we skipped payment related things as there's a hearing we want to adjourn + no other extras
                log.info("Payment for case {} skipped as requesting adjournment", caseDetails.getId());
            } else if (amountToPayShownToUser(caseDetails)) {
                try {
                    FeesData feesData = applicationsFeeCalculator.getFeeDataForAdditionalApplications(lastBundle);
                    paymentService.makePaymentForAdditionalApplications(caseDetails.getId(), caseData, feesData);
                } catch (Exception paymentException) {
                    log.error("Additional applications payment for case {} failed", caseDetails.getId());
                    publishEvent(new FailedPBAPaymentEvent(caseData,
                        uploadAdditionalApplicationsService.getApplicationTypes(lastBundle),
                        applicantsListGenerator.getApplicant(caseData, lastBundle)));
                }
            } else if (NO.getValue().equals(caseDetails.getData().get(DISPLAY_AMOUNT_TO_PAY))) {
                log.error("Additional applications payment for case {} not taken as payment fee not shown to user",
                    caseDetails.getId());
                publishEvent(new FailedPBAPaymentEvent(caseData,
                    uploadAdditionalApplicationsService.getApplicationTypes(lastBundle),
                    applicantsListGenerator.getApplicant(caseData, lastBundle)));
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
