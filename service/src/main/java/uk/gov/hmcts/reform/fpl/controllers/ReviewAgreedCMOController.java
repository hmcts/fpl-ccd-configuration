package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.CaseManagementOrderService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Api
@RestController
@RequestMapping("/callback/review-cmo")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewAgreedCMOController {

    private final ObjectMapper mapper;
    private final CaseManagementOrderService cmoService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

//        List<Element<CaseManagementOrder>> draftCMOs = defaultIfNull(caseData.getDraftUploadedCMOs(),
//            Collections.emptyList());

        List<Element<CaseManagementOrder>> draftCMOs = buildDraftCMOs();

        List<Element<CaseManagementOrder>> cmosReadyForApproval = draftCMOs.stream().filter(
            cmo -> cmo.getValue().getStatus().equals(SEND_TO_JUDGE)).collect(Collectors.toList());

        if (cmosReadyForApproval.size() > 1) {
            caseDetails.getData().put("numDraftCMOs", "MULTI");
            caseDetails.getData().put("cmoToReviewList", cmoService.buildDynamicList(cmosReadyForApproval));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        Object dynamicList = caseData.getCmoToReviewList();
        UUID selectedCMOCode = dynamicList instanceof String ? UUID.fromString(dynamicList.toString()) :
            mapper.convertValue(dynamicList, DynamicList.class).getValueCode();
        CaseManagementOrder selectedCMO = buildDraftCMOs().stream()
            .filter(element -> element.getId().equals(selectedCMOCode))
            .findFirst()
            .map(Element::getValue).orElse(CaseManagementOrder.builder().build());

        caseData.getReviewCMODecision().setDocument(selectedCMO.getOrder());
        caseDetails.getData().put("reviewCMODecision",
            caseData.getReviewCMODecision().getDocument());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private List<Element<CaseManagementOrder>> buildDraftCMOs() {
        return List.of(element(UUID.fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"), CaseManagementOrder.builder()
                .hearing("Case management hearing 12th August 2020")
                .dateSent(LocalDate.now().plusDays(10))
                .order(DocumentReference.builder()
                    .filename(randomAlphanumeric(10))
                    .url(randomAlphanumeric(10))
                    .binaryUrl(randomAlphanumeric(10))
                    .build())
                .status(SEND_TO_JUDGE).build()),
            element(UUID.fromString("c15eb00f-e151-47f2-8e5f-374cc6fc2657"), CaseManagementOrder.builder()
                .hearing("Issue resolution hearing 20th October 2020")
                .dateSent(LocalDate.now().plusDays(10))
                .order(DocumentReference.builder()
                    .filename(randomAlphanumeric(10))
                    .url(randomAlphanumeric(10))
                    .binaryUrl(randomAlphanumeric(10))
                    .build())
                .status(SEND_TO_JUDGE).build()));
    }
}
