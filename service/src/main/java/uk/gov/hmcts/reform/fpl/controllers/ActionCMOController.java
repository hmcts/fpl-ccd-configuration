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
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.ActionCmoService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;

@Api
@RestController
@RequestMapping("/callback/action-cmo")
public class ActionCMOController {
    private final DraftCMOService draftCMOService;
    private final ActionCmoService actionCmoService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final ObjectMapper mapper;

    public ActionCMOController(DraftCMOService draftCMOService,
                               ActionCmoService actionCmoService,
                               DocmosisDocumentGeneratorService docmosisDocumentGeneratorService,
                               UploadDocumentService uploadDocumentService,
                               ObjectMapper mapper) {
        this.draftCMOService = draftCMOService;
        this.actionCmoService = actionCmoService;
        this.docmosisDocumentGeneratorService = docmosisDocumentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
        this.mapper = mapper;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        draftCMOService.prepareCustomDirections(caseDetails.getData());

        caseDetails.getData().put("nextHearingDateList", getHearingDynamicList(caseData.getHearingDetails()));

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

        Document document = getDocument(authorization, userId, caseDetails.getData(), false);

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

        Document document = getDocument(authorization, userId, caseDetails.getData(), hasJudgeApproved(order));

        CaseManagementOrder orderWithDocument = actionCmoService.addDocument(order, document);

        CaseManagementOrder orderWithNextHearing =
            actionCmoService.appendNextHearingDateToCMO(caseData.getNextHearingDateList(), orderWithDocument);

        actionCmoService.prepareCaseDetailsForSubmission(caseDetails, orderWithNextHearing, hasJudgeApproved(order));

        String nextHearingDate =
            actionCmoService.createNextHearingDateLabel(orderWithNextHearing, caseData.getHearingDetails());

        caseDetails.getData().put("nextHearingDateLabel", nextHearingDate);

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
        return SEND_TO_ALL_PARTIES.equals(caseManagementOrder.getAction().getType());
    }

    private DynamicList getHearingDynamicList(List<Element<HearingBooking>> hearingBookings) {
        return draftCMOService.getHearingDateDynamicList(hearingBookings, null);
    }
}
