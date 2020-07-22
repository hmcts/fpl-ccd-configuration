package uk.gov.hmcts.reform.fpl.controllers.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderReadyForPartyReviewEvent;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.exceptions.CMOCodeNotFound;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.CaseManagementOrderService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder.sealFrom;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Api
@RestController
@RequestMapping("/callback/review-cmo")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewAgreedCMOController {

    private final Time time;
    private final ObjectMapper mapper;
    private final CaseManagementOrderService cmoService;
    private final DocumentSealingService documentSealingService;
    private final ApplicationEventPublisher eventPublisher;
    private final DocumentDownloadService documentDownloadService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<CaseManagementOrder>> draftCMOs = defaultIfNull(caseData.getDraftUploadedCMOs(),
            Collections.emptyList());

        List<Element<CaseManagementOrder>> cmosReadyForApproval = draftCMOs.stream().filter(
            cmo -> cmo.getValue().getStatus().equals(SEND_TO_JUDGE)).collect(Collectors.toList());

        if (cmosReadyForApproval.size() > 1) {
            data.put("numDraftCMOs", "MULTI");
            data.put("cmoToReviewList", cmoService.buildDynamicListCMO(cmosReadyForApproval));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        Object dynamicList = caseData.getCmoToReviewList();
        UUID selectedCMOCode = dynamicList instanceof String ? UUID.fromString(dynamicList.toString()) :
            mapper.convertValue(dynamicList, DynamicList.class).getValueCode();
        Element<CaseManagementOrder> selectedCMO = caseData.getDraftUploadedCMOs().stream()
            .filter(element -> element.getId().equals(selectedCMOCode))
            .findFirst()
            .orElseThrow(() -> new CMOCodeNotFound("Could not find draft cmo with id " + selectedCMOCode));

        data.put("reviewCMODecision", ReviewDecision.builder().document(selectedCMO.getValue().getOrder()).build());

        List<Element<CaseManagementOrder>> draftCMOs = caseData.getDraftUploadedCMOs();

        List<Element<CaseManagementOrder>> cmosReadyForApproval = draftCMOs.stream().filter(
            cmo -> cmo.getValue().getStatus().equals(SEND_TO_JUDGE)).collect(Collectors.toList());

        if (!(dynamicList instanceof DynamicList)) {
            // reconstruct dynamic list
            data.put("cmoToReviewList", cmoService.buildDynamicListCMO(cmosReadyForApproval, selectedCMOCode));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest)
        throws Exception {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        Object dynamicList = caseData.getCmoToReviewList();

        UUID selectedCMOCode = dynamicList instanceof String ? UUID.fromString(dynamicList.toString()) :
            mapper.convertValue(dynamicList, DynamicList.class).getValueCode();

        Element<CaseManagementOrder> cmo = caseData.getDraftUploadedCMOs().stream()
            .filter(element -> element.getId().equals(selectedCMOCode))
            .findFirst()
            .orElseThrow(() -> new CMOCodeNotFound("Could not find draft cmo with id " + selectedCMOCode));

        if (CMOReviewOutcome.SEND_TO_ALL_PARTIES.equals(caseData.getReviewCMODecision().getDecision())) {
            caseData.getDraftUploadedCMOs().remove(cmo);

            Element<HearingBooking> cmoHearing = caseData.getHearingDetails()
                .stream()
                .filter(hearing -> cmo.getId().equals(hearing.getValue().getCaseManagementOrderId()))
                .findFirst()
                .orElseThrow(NoHearingBookingException::new);

            Element<CaseManagementOrder> cmoToSeal = element(sealFrom(cmo.getValue().getOrder(),
                cmoHearing.getValue(), time.now().toLocalDate()));

            DocumentReference sealedDocument = documentSealingService.sealDocument(cmoToSeal.getValue().getOrder());
            cmoToSeal.getValue().setOrder(sealedDocument);

            List<Element<CaseManagementOrder>> sealedCMOs = caseData.getSealedCMOs();
            sealedCMOs.add(cmoToSeal);

            data.put("sealedCMOs", sealedCMOs);
        } else {
            cmo.getValue().setStatus(RETURNED);
        }

        data.put("draftUploadedCMOs", caseData.getDraftUploadedCMOs());
        data.remove("numDraftCMOs");
        data.remove("cmoToReviewList");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseDataBefore = mapper.convertValue(callbackRequest.getCaseDetailsBefore().getData(), CaseData.class);
        CaseData caseData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);

        if (CMOReviewOutcome.SEND_TO_ALL_PARTIES.equals(caseData.getReviewCMODecision().getDecision())) {
            CaseManagementOrder sealed = caseData.getSealedCMOs().get(caseData.getSealedCMOs().size() - 1).getValue();
            sendSealedCMO(callbackRequest, sealed);
        } else {
            List<Element<CaseManagementOrder>> draftCMOsBefore = caseDataBefore.getDraftUploadedCMOs();
            List<Element<CaseManagementOrder>> draftCMOs = caseData.getDraftUploadedCMOs();

            draftCMOs.removeAll(draftCMOsBefore);
            CaseManagementOrder returned = draftCMOs.get(0).getValue();
            sendReturnedCMO(callbackRequest, returned);
        }
    }

    private void sendSealedCMO(CallbackRequest callbackRequest, CaseManagementOrder cmo) {
        eventPublisher.publishEvent(new CaseManagementOrderReadyForPartyReviewEvent(callbackRequest,
            documentDownloadService.downloadDocument(cmo.getOrder().getBinaryUrl())));
    }

    private void sendReturnedCMO(CallbackRequest callbackRequest, CaseManagementOrder cmo) {
        eventPublisher.publishEvent(new CaseManagementOrderRejectedEvent(callbackRequest));
    }
}
