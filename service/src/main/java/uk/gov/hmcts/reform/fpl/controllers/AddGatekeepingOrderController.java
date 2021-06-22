package uk.gov.hmcts.reform.fpl.controllers;

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
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.GatekeepingOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.GatekeepingOrderSealDecision;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.service.GatekeepingOrderService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfProceedingsService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.sdo.GatekeepingOrderEventNotificationDecider;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Api
@RestController
@RequestMapping("/callback/add-gatekeeping-order")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AddGatekeepingOrderController extends CallbackController {
    private final CoreCaseDataService coreCaseDataService;
    private final GatekeepingOrderEventNotificationDecider notificationDecider;
    private final NoticeOfProceedingsService nopService;
    private final GatekeepingOrderService service;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);

        if (caseData.getAllocatedJudge() != null) {
            data.put("gatekeepingOrderIssuingJudge", service.setAllocatedJudgeLabel(caseData.getAllocatedJudge(),
                caseData.getGatekeepingOrderEventData().getGatekeepingOrderIssuingJudge()));
        }

        service.getHearing(caseData)
            .map(HearingBooking::getStartDate)
            .map(hearingDate -> formatLocalDateTimeBaseUsingFormat(hearingDate, DATE_TIME))
            .ifPresent(hearingDate -> {
                data.put("gatekeepingOrderHasHearing1", YesNo.YES);
                data.put("gatekeepingOrderHasHearing2", YesNo.YES);
                data.put("gatekeepingOrderHearingDate1", hearingDate);
                data.put("gatekeepingOrderHearingDate2", hearingDate);
            });

        return respond(caseDetails);
    }

    @PostMapping("/direction-selection/mid-event")
    public AboutToStartOrSubmitCallbackResponse populateSelectedDirections(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();

        service.populateStandardDirections(caseDetails);

        return respond(caseDetails);
    }

    @PostMapping("/generate-draft/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleGenerateDraftMidEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        CaseData caseData = service.updateStandardDirections(caseDetails);

        caseDetails.getData().put("gatekeepingOrderSealDecision", service.buildSealDecisionPage(caseData));
        caseDetails.getData().put("standardDirections", caseData.getGatekeepingOrderEventData()
            .getStandardDirections());

        return respond(caseDetails);
    }

    //Need to redesign for upload + urgent upload routes
    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = service.updateStandardDirections(caseDetails);
        GatekeepingOrderEventData eventData = caseData.getGatekeepingOrderEventData();

        StandardDirectionOrder gatekeepingOrder = service.buildBaseGatekeepingOrder(caseData);

        GatekeepingOrderSealDecision gatekeepingOrderSealDecision = eventData.getGatekeepingOrderSealDecision();

        if (gatekeepingOrderSealDecision.getOrderStatus() == SEALED) {
            //generate document
            Document document = service.buildDocument(caseData);

            gatekeepingOrder = gatekeepingOrder.toBuilder()
                .dateOfIssue(formatLocalDateToString(gatekeepingOrderSealDecision.getDateOfIssue(), DATE))
                .unsealedDocumentCopy(gatekeepingOrderSealDecision.getDraftDocument())
                .orderDoc(buildFromDocument(document))
                .build();

            List<DocmosisTemplates> docmosisTemplateTypes = service.getNoticeOfProceedingsTemplates(caseData);

            List<Element<DocumentBundle>> nop = nopService.uploadAndPrepareNoticeOfProceedingBundle(
                caseData, docmosisTemplateTypes
            );

            caseDetails.getData().put("noticeOfProceedingsBundle", nop);

            caseDetails.getData().put("state", CASE_MANAGEMENT);

            removeTemporaryFields(caseDetails, "gatekeepingOrderRouter", "customDirections",
                "gatekeepingOrderIssuingJudge", "gatekeepingOrderSealDecision", "gatekeepingOrderHearingDate1",
                "gatekeepingOrderHearingDate2", "gatekeepingOrderHasHearing1", "gatekeepingOrderHasHearing2");

        } else {
            //no need to regenerate draft
            gatekeepingOrder = gatekeepingOrder.toBuilder()
                .orderDoc(gatekeepingOrderSealDecision.getDraftDocument())
                .build();
        }

        caseDetails.getData().put("standardDirectionOrder", gatekeepingOrder);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest request) {
        CaseData caseData = getCaseData(request);
        CaseData caseDataBefore = getCaseDataBefore(request);

        Optional<GatekeepingOrderEvent> event = notificationDecider.buildEventToPublish(
            caseData, caseDataBefore.getState()
        );

        event.ifPresent(eventToPublish -> {
            coreCaseDataService.triggerEvent(
                JURISDICTION,
                CASE_TYPE,
                caseData.getId(),
                "internal-change-SEND_DOCUMENT",
                Map.of("documentToBeSent", eventToPublish.getOrder())
            );

            publishEvent(eventToPublish);
        });
    }
}
