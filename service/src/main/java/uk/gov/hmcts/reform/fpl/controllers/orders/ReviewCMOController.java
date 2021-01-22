package uk.gov.hmcts.reform.fpl.controllers.orders;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.cmo.DraftOrderService;
import uk.gov.hmcts.reform.fpl.service.cmo.ReviewCMOService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_REQUESTED_CHANGES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Api
@RestController
@RequestMapping("/callback/review-cmo")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewCMOController extends CallbackController {

    private final ReviewCMOService reviewCMOService;
    private final DocumentSealingService documentSealingService;
    private final CoreCaseDataService coreCaseDataService;
    private final DraftOrderService draftOrderService;
    private final Time time;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().remove("reviewCMODecision");
        caseDetails.getData().putAll(reviewCMOService.getPageDisplayControls(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(reviewCMOService.populateDraftOrdersData(caseData));

        if (!(caseData.getCmoToReviewList() instanceof DynamicList)) {
            // reconstruct dynamic list
            caseDetails.getData().put("cmoToReviewList", reviewCMOService.buildDynamicList(caseData));
        }

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        Element<HearingOrdersBundle> selectedOrdersBundle =
            reviewCMOService.getSelectedHearingDraftOrdersBundle(caseData);
        List<Element<HearingOrder>> ordersInBundle = selectedOrdersBundle.getValue().getOrders();

        List<Element<HearingOrder>> draftUploadedCMOs = caseData.getDraftUploadedCMOs();
        List<Element<HearingOrder>> sealedCMOs = caseData.getSealedCMOs();
        List<Element<GeneratedOrder>> orderCollection = caseData.getOrderCollection();

        if (!ordersInBundle.isEmpty()) {
            List<Element<ReviewDecision>> ordersReviewDecisions = caseData.getReviewCMODecision();

            ordersReviewDecisions.forEach(reviewDecisionElement -> {
                Element<HearingOrder> orderElement = ordersInBundle.stream()
                    .filter(element -> element.getId().equals(reviewDecisionElement.getId()))
                    .findFirst()
                    .orElseThrow(() -> new CMONotFoundException("CMO not found"));

                if (!JUDGE_REQUESTED_CHANGES.equals(reviewDecisionElement.getValue().getDecision())) {
                    Element<HearingOrder> orderToSeal = reviewCMOService.getCMOToSeal(reviewDecisionElement, orderElement);

                    orderToSeal.getValue().setLastUploadedOrder(orderToSeal.getValue().getOrder());
                    orderToSeal.getValue().setOrder(documentSealingService.sealDocument(orderToSeal.getValue().getOrder()));
                    if (orderToSeal.getValue().getType().isCmo()) {
                        draftUploadedCMOs.removeIf(cmo -> cmo.getId().equals(orderElement.getId()));
                        sealedCMOs.add(orderToSeal);
                        data.put("state", reviewCMOService.getStateBasedOnNextHearing(
                            caseData, reviewDecisionElement.getValue(), orderElement.getId()));
                    } else {
                        orderCollection.add(element(GeneratedOrder.builder()
                            .title(orderToSeal.getValue().getTitle())
                            .document(orderToSeal.getValue().getOrder())
                            .dateOfIssue(formatLocalDateToString(orderToSeal.getValue().getDateIssued(), DATE))
                            .judgeAndLegalAdvisor(null) // TODO: set judge and legal advisor
                            .date(formatLocalDateTimeBaseUsingFormat(time.now(), TIME_DATE))
                            //.children(getChildren(BLANK_ORDER, caseData)) //TODO
                            .build()));
                    }

                    ordersInBundle.removeIf(order -> order.getId().equals(orderElement.getId()));
                } else {
                    // remove rejected orders from hearing orders bundle
                    orderElement.getValue().toBuilder()
                        .requestedChanges(reviewDecisionElement.getValue().getChangesRequestedByJudge()).build();

                    ordersInBundle.removeIf(order -> order.getId().equals(orderElement.getId()));
                    if (orderElement.getValue().getType().isCmo()) {
                        draftUploadedCMOs.removeIf(cmo -> cmo.getId().equals(orderElement.getId()));
                    }
                }
            });
        }

        List<Element<HearingOrdersBundle>> updatedHearingDraftOrdersBundle =
            reviewCMOService.updateHearingDraftOrdersBundle(caseData, selectedOrdersBundle, ordersInBundle);

        data.put("sealedCMOs", sealedCMOs);
        data.put("orderCollection", orderCollection);
        data.put("draftUploadedCMOs", draftUploadedCMOs);
        data.put("hearingOrdersBundlesDrafts", updatedHearingDraftOrdersBundle);
        data.remove("numDraftCMOs");
        data.remove("cmoToReviewList");
        data.remove("reviewDraftOrdersTitles");

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);
        CaseData caseData = getCaseData(callbackRequest);

        //Checks caseDataBefore as caseData has been modified by this point
        List<Element<HearingOrder>> cmosReadyForApproval = reviewCMOService.getCMOsReadyForApproval(
            caseDataBefore);

        //TODO: check draft orders collection and reviewCMODecision for the rejected orders notification
        if (!cmosReadyForApproval.isEmpty()) {
            if (!JUDGE_REQUESTED_CHANGES.equals(caseData.getReviewCMODecision().get(0).getValue().getDecision())) {
                HearingOrder sealed = reviewCMOService.getLatestSealedCMO(caseData); // TODO fix
                DocumentReference documentToBeSent = sealed.getOrder();

                coreCaseDataService.triggerEvent(
                    callbackRequest.getCaseDetails().getJurisdiction(),
                    callbackRequest.getCaseDetails().getCaseTypeId(),
                    callbackRequest.getCaseDetails().getId(),
                    "internal-change-SEND_DOCUMENT",
                    Map.of("documentToBeSent", documentToBeSent)
                );

                publishEvent(new CaseManagementOrderIssuedEvent(caseData, sealed));
            } else {
                List<Element<HearingOrder>> draftCMOsBefore = caseDataBefore.getDraftUploadedCMOs();
                List<Element<HearingOrder>> draftCMOs = caseData.getDraftUploadedCMOs();

                //Get the CMO that was modified (status changed from READY -> RETURNED)
                draftCMOs.removeAll(draftCMOsBefore);
                HearingOrder cmoToReturn = draftCMOs.get(0).getValue();

                publishEvent(new CaseManagementOrderRejectedEvent(caseData, cmoToReturn));
            }
        }
    }
}
