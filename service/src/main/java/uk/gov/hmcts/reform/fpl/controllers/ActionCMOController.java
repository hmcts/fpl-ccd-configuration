package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.service.ActionCmoService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.io.IOException;

import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.Type.SEND_TO_ALL_PARTIES;

@Api
@RestController
@RequestMapping("/callback/action-cmo")
public class ActionCMOController {
    private static final String CMO_ACTION_KEY = "caseManagementOrderAction";
    private static final String CMO_KEY = "caseManagementOrder";

    private final DraftCMOService draftCMOService;
    private final ActionCmoService actionCmoService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final HearingBookingService hearingBookingService;
    private final ObjectMapper mapper;

    public ActionCMOController(DraftCMOService draftCMOService,
                               ActionCmoService actionCmoService,
                               DocmosisDocumentGeneratorService docmosisDocumentGeneratorService,
                               ObjectMapper mapper,
                               HearingBookingService hearingBookingService,
                               UploadDocumentService uploadDocumentService) {
        this.draftCMOService = draftCMOService;
        this.actionCmoService = actionCmoService;
        this.docmosisDocumentGeneratorService = docmosisDocumentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
        this.mapper = mapper;
        this.hearingBookingService = hearingBookingService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseDataMap = caseDetails.getData();

        draftCMOService.prepareCustomDirections(caseDetails.getData());

        CaseManagementOrder orderForAction = actionCmoService.getCaseManagementOrderForAction(caseDataMap);

        caseDetails.getData().put(CMO_KEY, orderForAction);

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

        CaseManagementOrder orderForAction = actionCmoService.getCaseManagementOrderForAction(caseDetails.getData());

        Document document = getDocument(authorization, userId, caseDetails.getData(), false);

        CaseManagementOrder orderWithDocument = actionCmoService.addDocument(orderForAction, document);

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

        CaseManagementOrder order = actionCmoService.getCaseManagementOrderForAction(caseDetails.getData());

        Document actionedCaseManageOrderDocument =
            getDocument(authorization, userId, caseDetails.getData(), hasJudgeApproved(order));

        CaseManagementOrder orderWithDocument = actionCmoService.addDocument(order, actionedCaseManageOrderDocument);

        prepareCaseDetailsForSubmission(caseDetails, orderWithDocument, hasJudgeApproved(order));

        setNextHearingDateLabel(caseDetails);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private void prepareCaseDetailsForSubmission(CaseDetails caseDetails, CaseManagementOrder order, boolean approved) {
        caseDetails.getData().put(CMO_ACTION_KEY, order.getAction());

        if (approved) {
            caseDetails.getData().put(CMO_KEY, order);
        } else {
            caseDetails.getData().remove(CMO_KEY);
        }
    }

    private Document getDocument(String authorization, String userId, Map<String, Object> caseData, boolean approved)
        throws IOException {
        Map<String, Object> cmoDocumentTemplateData = draftCMOService.generateCMOTemplateData(caseData);

        DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(
            cmoDocumentTemplateData, DocmosisTemplates.CMO);

        String documentTitle = (approved ? document.getDocumentTitle() : "draft-" + document.getDocumentTitle());

        return uploadDocumentService.uploadPDF(userId, authorization, document.getBytes(), documentTitle);
    }

    private boolean hasJudgeApproved(final CaseManagementOrder caseManagementOrder) {
        return caseManagementOrder.getAction().getType().equals(SEND_TO_ALL_PARTIES);
    }

    private void setNextHearingDateLabel(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        String nextHearingLabel = "";

        if (caseData.getCaseManagementOrder() != null
            && caseData.getCaseManagementOrder().getAction() != null) {
            UUID nextHearingId = caseData.getCaseManagementOrder().getAction().getNextHearingId();

            HearingBooking hearingBooking =
                hearingBookingService.getHearingBookingByUUID(caseData.getHearingDetails(), nextHearingId);

            nextHearingLabel = actionCmoService.formatHearingBookingLabel(hearingBooking);
        }

        caseDetails.getData().put("nextHearingDateLabel", nextHearingLabel);
    }
}
