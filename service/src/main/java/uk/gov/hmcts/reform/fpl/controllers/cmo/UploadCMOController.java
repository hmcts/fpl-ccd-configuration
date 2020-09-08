package uk.gov.hmcts.reform.fpl.controllers.cmo;

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
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.events.cmo.NewCMOUploaded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.cmo.UploadCMOService;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@Api
@RestController
@RequestMapping("/callback/upload-cmo")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UploadCMOController extends CallbackController {
    private static final String[] TRANSIENT_FIELDS = {
        "uploadedCaseManagementOrder", "hearingsWithoutApprovedCMO", "cmoJudgeInfo", "cmoHearingInfo",
        "numHearingsWithoutCMO", "singleHearingWithCMO", "multiHearingsWithCMOs", "showHearingsSingleTextArea",
        "showHearingsMultiTextArea"
    };

    private final UploadCMOService cmoService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData()
            .putAll(cmoService.getInitialPageData(caseData.getPastHearings(), caseData.getDraftUploadedCMOs()));

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleLabelMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        // handle document fields being set to null when clicking previous button
        DocumentReference uploadedCMO = caseData.getUploadedCaseManagementOrder();
        if (uploadedCMO != null && uploadedCMO.isEmpty()) {
            caseDetails.getData().remove("uploadedCaseManagementOrder");
        }

        // update judge and hearing labels
        caseDetails.getData().putAll(cmoService.prepareJudgeAndHearingDetails(
            caseData.getHearingsWithoutApprovedCMO(),
            caseData.getPastHearings(),
            caseData.getDraftUploadedCMOs()
        ));

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        List<Element<CaseManagementOrder>> draftCMOs = caseData.getDraftUploadedCMOs();
        DocumentReference uploadedCaseManagementOrder = caseData.getUploadedCaseManagementOrder();

        if (uploadedCaseManagementOrder != null) {
            List<Element<HearingBooking>> hearings = caseData.getPastHearings();
            cmoService.updateHearingsAndUnsealedCMOs(
                hearings,
                draftCMOs,
                uploadedCaseManagementOrder,
                caseData.getHearingsWithoutApprovedCMO()
            );
            // update case data
            caseDetails.getData().put("draftUploadedCMOs", draftCMOs);
            caseDetails.getData().put("hearingDetails", hearings);
        }
        // remove transient fields
        removeTemporaryFields(caseDetails, TRANSIENT_FIELDS);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handelSubmitted(@RequestBody CallbackRequest request) {
        CaseData caseDataBefore = getCaseDataBefore(request);
        CaseData caseData = getCaseData(request);

        if (cmoService.isNewCmoUploaded(caseData.getDraftUploadedCMOs(), caseDataBefore.getDraftUploadedCMOs())) {
            List<Element<HearingBooking>> hearings = caseData.getHearingDetails();
            List<Element<HearingBooking>> hearingsBefore = caseDataBefore.getHearingDetails();
            hearings.removeAll(hearingsBefore);

            publishEvent(new NewCMOUploaded(caseData, hearings.get(0).getValue()));
        }
    }
}
