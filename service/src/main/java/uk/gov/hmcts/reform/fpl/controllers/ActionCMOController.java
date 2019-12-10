package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.events.CMOEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.service.ActionCmoService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.io.IOException;

import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;

@Api
@RestController
@RequestMapping("/callback/action-cmo")
public class ActionCMOController {
    private final DraftCMOService draftCMOService;
    private final ActionCmoService actionCmoService;
    private final UploadDocumentService uploadDocumentService;
    private final ObjectMapper mapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ActionCMOController(DraftCMOService draftCMOService,
                               ActionCmoService actionCmoService,
                               UploadDocumentService uploadDocumentService,
                               ObjectMapper mapper,
                               ApplicationEventPublisher applicationEventPublisher) {
        this.draftCMOService = draftCMOService;
        this.actionCmoService = actionCmoService;
        this.uploadDocumentService = uploadDocumentService;
        this.mapper = mapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        draftCMOService.prepareCustomDirections(caseDetails.getData());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) throws IOException {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        CaseManagementOrder order = caseData.getCaseManagementOrder();

        caseDetails.getData()
            .putAll(actionCmoService.extractMapFieldsFromCaseManagementOrder(order, caseData.getHearingDetails()));

        Document document = getDocument(authorization, userId, caseData, false);

        CaseManagementOrder orderWithDocument = actionCmoService.addDocument(order, document);

        caseDetails.getData().put("orderAction", ImmutableMap.of("orderDoc", orderWithDocument.getOrderDoc()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) throws IOException {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        CaseManagementOrder order = caseData.getCaseManagementOrder().toBuilder()
            .action(caseData.getOrderAction())
            .build();

        caseDetails.getData()
            .putAll(actionCmoService.extractMapFieldsFromCaseManagementOrder(order, caseData.getHearingDetails()));

        Document document = getDocument(authorization, userId, caseData, hasJudgeApproved(order));

        CaseManagementOrder orderWithDocument = actionCmoService.addDocument(order, document);

        actionCmoService.prepareCaseDetailsForSubmission(caseDetails, orderWithDocument, hasJudgeApproved(order));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestHeader(value = "authorization") String authorization,
                                     @RequestHeader(value = "user-id") String userId,
                                     @RequestBody CallbackRequest callbackRequest) throws IOException {
        CaseData caseData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);

        DocmosisDocument docmosisDocument = actionCmoService.getDocmosisDocument(caseData,
            hasJudgeApproved(caseData.getCaseManagementOrder()));

        applicationEventPublisher.publishEvent(new CMOEvent(callbackRequest, authorization, userId, docmosisDocument));
    }

    private Document getDocument(String authorization, String userId, CaseData data, boolean approved)
        throws IOException {
        DocmosisDocument document = actionCmoService.getDocmosisDocument(data, approved);

        return uploadDocumentService.uploadPDF(userId, authorization, document.getBytes(), document.getDocumentTitle());
    }

    private boolean hasJudgeApproved(CaseManagementOrder caseManagementOrder) {
        return SEND_TO_ALL_PARTIES.equals(caseManagementOrder.getAction().getType());
    }
}
