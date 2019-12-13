package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.service.CMODocmosisTemplateDataGenerationService;
import uk.gov.hmcts.reform.fpl.service.CaseManagementOrderService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.io.IOException;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderErrorMessages.HEARING_NOT_COMPLETED;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;

@Api
@RestController
@RequestMapping("/callback/action-cmo")
public class ActionCaseManagementOrderController {
    private final DraftCMOService draftCMOService;
    private final CaseManagementOrderService caseManagementOrderService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final ObjectMapper mapper;
    private final CMODocmosisTemplateDataGenerationService templateDataGenerationService;
    private final CoreCaseDataService coreCaseDataService;

    public ActionCaseManagementOrderController(DraftCMOService draftCMOService,
                                               CaseManagementOrderService caseManagementOrderService,
                                               DocmosisDocumentGeneratorService docmosisDocumentGeneratorService,
                                               UploadDocumentService uploadDocumentService,
                                               ObjectMapper mapper,
                                               CMODocmosisTemplateDataGenerationService templateDataGenerationService,
                                               CoreCaseDataService coreCaseDataService) {
        this.draftCMOService = draftCMOService;
        this.caseManagementOrderService = caseManagementOrderService;
        this.docmosisDocumentGeneratorService = docmosisDocumentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
        this.mapper = mapper;
        this.templateDataGenerationService = templateDataGenerationService;
        this.coreCaseDataService = coreCaseDataService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        caseDetails.getData()
            .putAll(caseManagementOrderService.extractMapFieldsFromCaseManagementOrder(caseData.getCmoToAction()));

        draftCMOService.prepareCustomDirections(caseDetails, caseData.getCmoToAction());

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

        Document document = getDocument(authorization, userId, caseData, true);

        caseDetails.getData().put("orderAction", OrderAction.builder().document(buildFromDocument(document)).build());

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

        if (sendToAllPartiesBeforeHearingDate(caseData)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(ImmutableList.of(HEARING_NOT_COMPLETED.getValue()))
                .build();
        }

        CaseManagementOrder order = caseData.getCmoToAction();

        order = draftCMOService.prepareCMO(caseData, order).toBuilder()
            .id(order.getId())
            .hearingDate(order.getHearingDate())
            .build();

        OrderAction orderAction = caseManagementOrderService.removeDocumentFromOrderAction(caseData.getOrderAction());

        order = caseManagementOrderService.addAction(order, orderAction);

        Document document = getDocument(authorization, userId, caseData, order.isDraft());

        order = caseManagementOrderService.addDocument(order, document);

        caseDetails.getData().put("cmoToAction", order);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        coreCaseDataService.triggerEvent(
            callbackRequest.getCaseDetails().getJurisdiction(),
            callbackRequest.getCaseDetails().getCaseTypeId(),
            callbackRequest.getCaseDetails().getId(),
            "internal-change:CMO_PROGRESSION"
        );
    }

    private boolean sendToAllPartiesBeforeHearingDate(CaseData caseData) {
        return caseData.getOrderAction().getType() == SEND_TO_ALL_PARTIES
            && caseManagementOrderService.isHearingDateInFuture(caseData);
    }

    private Document getDocument(String auth, String userId, CaseData data, boolean draft) throws IOException {
        Map<String, Object> cmoDocumentTemplateData = templateDataGenerationService.getTemplateData(data, draft);

        DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(
            cmoDocumentTemplateData, DocmosisTemplates.CMO);

        String documentTitle = (draft ? "draft-" + document.getDocumentTitle() : document.getDocumentTitle());

        return uploadDocumentService.uploadPDF(userId, auth, document.getBytes(), documentTitle);
    }
}
