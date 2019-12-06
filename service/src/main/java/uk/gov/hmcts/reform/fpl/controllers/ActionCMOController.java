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
import uk.gov.hmcts.reform.fpl.enums.CMOActionType;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrderAction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.service.CaseManageOrderActionService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Api
@RestController
@RequestMapping("/callback/action-cmo")
public class ActionCMOController {
    private static final String CASE_MANAGEMENT_ORDER_ACTION_KEY = "caseManagementOrderAction";
    private static final String CASE_MANAGEMENT_ORDER_KEY = "caseManagementOrder";
    private final DraftCMOService draftCMOService;
    private final CaseManageOrderActionService caseManageOrderActionService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final HearingBookingService hearingBookingService;
    private final ObjectMapper mapper;

    public ActionCMOController(DraftCMOService draftCMOService,
                               CaseManageOrderActionService caseManageOrderActionService,
                               DocmosisDocumentGeneratorService docmosisDocumentGeneratorService,
                               ObjectMapper mapper,
                               HearingBookingService hearingBookingService,
                               UploadDocumentService uploadDocumentService) {
        this.draftCMOService = draftCMOService;
        this.caseManageOrderActionService = caseManageOrderActionService;
        this.docmosisDocumentGeneratorService = docmosisDocumentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
        this.mapper = mapper;
        this.hearingBookingService = hearingBookingService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        draftCMOService.prepareCustomDirections(caseDetails.getData());

        CaseManagementOrder caseManagementOrder = draftCMOService.prepareCMO(caseData);

        caseDetails.getData().put(CASE_MANAGEMENT_ORDER_KEY, caseManagementOrder);

        populateHearingDynamicList(caseData);

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
        Map<String, Object> caseData = caseDetails.getData();

        CaseManagementOrder caseManagementOrderToBeActioned = draftCMOService.prepareCMO(caseData);

        Document updatedDraftCMODocument = getDocument(authorization, userId, caseData, false);

        CaseManagementOrder updatedDraftCaseManagementOrderWithDocument =
            caseManageOrderActionService.addDocumentToCaseManagementOrder(caseManagementOrderToBeActioned,
                updatedDraftCMODocument);

        caseDetails.getData().put(CASE_MANAGEMENT_ORDER_ACTION_KEY, ImmutableMap.of("orderDoc",
            updatedDraftCaseManagementOrderWithDocument.getOrderDoc()));

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
        CaseManagementOrder updatedDraftCaseManagementOrder = draftCMOService.prepareCMO(caseDetails.getData());

        boolean judgeApprovedDraftCMO = hasJudgeApprovedDraftCMO(updatedDraftCaseManagementOrder);

        Document actionedCaseManageOrderDocument = getDocument(authorization, userId, caseDetails.getData(),
            judgeApprovedDraftCMO);

        CaseManagementOrder updatedCaseManagementOrderWithDocument =
            caseManageOrderActionService.addDocumentToCaseManagementOrder(updatedDraftCaseManagementOrder,
                actionedCaseManageOrderDocument);

        CaseManagementOrderAction caseManagementOrderAction =
            updatedCaseManagementOrderWithDocument.getCaseManagementOrderAction();

        prepareCaseDetailsForSubmission(caseDetails, caseManagementOrderAction, updatedCaseManagementOrderWithDocument,
            judgeApprovedDraftCMO);

        setNextHearingDateLabel(caseDetails);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private void prepareCaseDetailsForSubmission(final CaseDetails caseDetails,
                                                 final CaseManagementOrderAction caseManagementOrderAction,
                                                 final CaseManagementOrder updatedDraftCaseManagementOrder,
                                                 final boolean judgeApprovedDraftCMO) {
        caseDetails.getData().put(CASE_MANAGEMENT_ORDER_ACTION_KEY, caseManagementOrderAction);
        if (judgeApprovedDraftCMO) {
            caseDetails.getData().put(CASE_MANAGEMENT_ORDER_KEY, updatedDraftCaseManagementOrder);
        } else {
            caseDetails.getData().remove(CASE_MANAGEMENT_ORDER_KEY);
        }
    }

    private Document getDocument(final String authorization, final String userId,
                                 final Map<String, Object> caseData,
                                 final boolean judgeApprovedDraftCMO) throws IOException {
        final Map<String, Object> cmoDocumentTemplateData = draftCMOService.generateCMOTemplateData(caseData);
        final DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(
            cmoDocumentTemplateData, DocmosisTemplates.CMO);
        final String documentTitle = (judgeApprovedDraftCMO
            ? document.getDocumentTitle() : "draft-" + document.getDocumentTitle());
        return uploadDocumentService.uploadPDF(userId, authorization, document.getBytes(), documentTitle);
    }

    private boolean hasJudgeApprovedDraftCMO(final CaseManagementOrder caseManagementOrder) {
        return CMOActionType.SEND_TO_ALL_PARTIES.equals(
            caseManagementOrder.getCaseManagementOrderAction().getCmoActionType());
    }

    private void setNextHearingDateLabel(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        String nextHearingLabel = "";

        if (caseData.getCaseManagementOrder() != null
            && caseData.getCaseManagementOrder().getCaseManagementOrderAction() != null) {
            UUID nextHearingId = caseData.getCaseManagementOrder().getCaseManagementOrderAction().getNextHearingId();

            HearingBooking hearingBooking =
                hearingBookingService.getHearingBookingByUUID(caseData.getHearingDetails(), nextHearingId);

            nextHearingLabel = caseManageOrderActionService.formatHearingBookingLabel(hearingBooking);
        }

        caseDetails.getData().put("nextHearingDateLabel", nextHearingLabel);
    }

    private void populateHearingDynamicList(Map<String, Object> caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails, CaseData.class);

        caseDetails.put("nextHearingDateList",
            draftCMOService.getHearingDateDynamicList(caseData.getHearingDetails(), null));
    }
}
