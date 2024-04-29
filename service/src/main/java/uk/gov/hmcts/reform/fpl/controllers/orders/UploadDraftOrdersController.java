package uk.gov.hmcts.reform.fpl.controllers.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersUploaded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.UploadDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundles;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.cmo.DraftOrderService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@RestController
@RequestMapping("/callback/upload-draft-orders")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UploadDraftOrdersController extends CallbackController {

    private static final int MAX_ORDERS = 10;
    private final DraftOrderService service;
    private final CaseConverter caseConverter;

    @PostMapping("/populate-initial-data/mid-event")
    public CallbackResponse handlePopulateInitialData(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = CaseDetailsMap.caseDetailsMap(caseDetails);

        caseDetailsMap.putIfNotEmpty(caseConverter.toMap(service.getInitialData(caseData)));

        return respond(caseDetailsMap);
    }

    @PostMapping("/populate-drafts-info/mid-event")
    public CallbackResponse handlePopulateDraftInfo(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = CaseDetailsMap.caseDetailsMap(caseDetails);

        caseDetailsMap.putIfNotEmpty(caseConverter.toMap(service.getDraftsInfo(caseData)));

        return respond(caseDetailsMap);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        UploadDraftOrdersData eventData = caseData.getUploadDraftOrdersEventData();

        if (isNotEmpty(eventData.getCurrentHearingOrderDrafts())
            && eventData.getCurrentHearingOrderDrafts().size() > MAX_ORDERS) {
            return respond(caseDetails, List.of(String.format("Maximum number of draft orders is %s", MAX_ORDERS)));
        }

        List<Element<HearingOrder>> unsealedCMOs = caseData.getDraftUploadedCMOs();
        List<Element<HearingBooking>> hearings = defaultIfNull(caseData.getHearingDetails(), new ArrayList<>());
        HearingOrdersBundles hearingOrdersBundles = service.migrateCmoDraftToOrdersBundles(caseData);

        Map<HearingOrderType, List<Element<HearingOrdersBundle>>> bundles = Map.of(
            DRAFT_CMO, hearingOrdersBundles.getDraftCmos(),
            AGREED_CMO, hearingOrdersBundles.getAgreedCmos(),
            C21, hearingOrdersBundles.getAgreedCmos()
        );

        UUID hearingId = service.updateCase(eventData, hearings, unsealedCMOs, bundles);

        // update case data
        caseDetails.getData().put("draftUploadedCMOs", unsealedCMOs);
        caseDetails.getData().put("hearingDetails", hearings);
        caseDetails.getData().put("hearingOrdersBundlesDrafts", bundles.get(AGREED_CMO));
        caseDetails.getData().put("hearingOrdersBundlesDraftReview", bundles.get(DRAFT_CMO));
        caseDetails.getData().put("lastHearingOrderDraftsHearingId", hearingId);

        removeTemporaryFields(caseDetails, UploadDraftOrdersData.temporaryFields());

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest request) {
        CaseData caseDataBefore = getCaseDataBefore(request);
        CaseData caseData = getCaseData(request);

        publishEvent(new DraftOrdersUploaded(caseData));
        publishEvent(new AfterSubmissionCaseDataUpdated(caseData, caseDataBefore));
    }
}
