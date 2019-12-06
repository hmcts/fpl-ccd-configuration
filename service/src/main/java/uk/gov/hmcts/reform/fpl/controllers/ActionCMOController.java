package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.service.ActionCmoService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.io.IOException;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.Type.SEND_TO_ALL_PARTIES;

@Api
@RestController
@RequestMapping("/callback/action-cmo")
public class ActionCMOController {
    private static final String CMO_ACTION_KEY = "orderAction";
    private static final String CMO_KEY = "caseManagementOrder";

    private final DraftCMOService draftCMOService;
    private final ActionCmoService actionCmoService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;

    public ActionCMOController(DraftCMOService draftCMOService,
                               ActionCmoService actionCmoService,
                               DocmosisDocumentGeneratorService docmosisDocumentGeneratorService,
                               UploadDocumentService uploadDocumentService) {
        this.draftCMOService = draftCMOService;
        this.actionCmoService = actionCmoService;
        this.docmosisDocumentGeneratorService = docmosisDocumentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseDataMap = caseDetails.getData();

        draftCMOService.prepareCustomDirections(caseDetails.getData());

        CaseManagementOrder order = actionCmoService.getCaseManagementOrder(caseDataMap);

        caseDetails.getData().put(CMO_KEY, order);

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

        CaseManagementOrder order = actionCmoService.getCaseManagementOrder(caseDetails.getData());

        Document document = getDocument(authorization, userId, caseDetails.getData(), false);

        CaseManagementOrder orderWithDocument = actionCmoService.addDocument(order, document);

        caseDetails.getData()
            .put(CMO_ACTION_KEY, ImmutableMap.of("orderDoc", orderWithDocument.getOrderDoc()));

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

        CaseManagementOrder order = actionCmoService.getCaseManagementOrder(caseDetails.getData());

        Document document = getDocument(authorization, userId, caseDetails.getData(), hasJudgeApproved(order));

        CaseManagementOrder orderWithDocument = actionCmoService.addDocument(order, document);

        actionCmoService.prepareCaseDetailsForSubmission(caseDetails, orderWithDocument, hasJudgeApproved(order));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private Document getDocument(String authorization, String userId, Map<String, Object> caseData, boolean approved)
        throws IOException {
        Map<String, Object> cmoDocumentTemplateData = draftCMOService.generateCMOTemplateData(caseData);

        DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(
            cmoDocumentTemplateData, DocmosisTemplates.CMO);

        String documentTitle = (approved ? document.getDocumentTitle() : "draft-" + document.getDocumentTitle());

        return uploadDocumentService.uploadPDF(userId, authorization, document.getBytes(), documentTitle);
    }

    private boolean hasJudgeApproved(CaseManagementOrder caseManagementOrder) {
        return caseManagementOrder.getAction().getType().equals(SEND_TO_ALL_PARTIES);
    }
}
