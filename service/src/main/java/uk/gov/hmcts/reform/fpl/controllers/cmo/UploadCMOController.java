package uk.gov.hmcts.reform.fpl.controllers.cmo;

import com.fasterxml.jackson.core.type.TypeReference;
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
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.events.cmo.NewCMOUploaded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.UploadCMOEventData;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.cmo.UploadCMOService;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@Api
@RestController
@RequestMapping("/callback/upload-cmo")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UploadCMOController extends CallbackController {

    private final UploadCMOService service;
    private final ObjectMapper mapper;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        UploadCMOEventData pageData = service.getInitialPageData(
            caseData.getPastHearings(), caseData.getDraftUploadedCMOs()
        );

        caseDetails.getData().putAll(mapper.convertValue(pageData, new TypeReference<>() {}));

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        UploadCMOEventData eventData = caseData.getUploadCMOEventData();

        // handle document fields being set to null when clicking previous button
        DocumentReference uploadedCMO = eventData.getUploadedCaseManagementOrder();
        if (uploadedCMO != null && uploadedCMO.isEmpty()) {
            caseDetails.getData().remove("uploadedCaseManagementOrder");
        }

        // update judge and hearing labels
        UploadCMOEventData judgeAndHearingDetails = service.prepareJudgeAndHearingDetails(
            eventData.getHearingsWithoutApprovedCMO(), caseData.getPastHearings(), caseData.getDraftUploadedCMOs()
        );

        caseDetails.getData().putAll(mapper.convertValue(judgeAndHearingDetails, new TypeReference<>() {}));

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        UploadCMOEventData eventData = caseData.getUploadCMOEventData();
        List<Element<CaseManagementOrder>> draftCMOs = caseData.getDraftUploadedCMOs();
        DocumentReference uploadedCaseManagementOrder = eventData.getUploadedCaseManagementOrder();

        if (uploadedCaseManagementOrder != null) {
            List<Element<HearingBooking>> hearings = caseData.getPastHearings();
            service.updateHearingsAndUnsealedCMOs(
                hearings, draftCMOs, uploadedCaseManagementOrder, eventData.getHearingsWithoutApprovedCMO()
            );
            // update case data
            caseDetails.getData().put("draftUploadedCMOs", draftCMOs);
            caseDetails.getData().put("hearingDetails", hearings);
        }

        // remove transient fields
        removeTemporaryFields(caseDetails, UploadCMOEventData.transientFields());

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest request) {
        CaseData caseDataBefore = getCaseDataBefore(request);
        CaseData caseData = getCaseData(request);

        if (service.isNewCmoUploaded(caseData.getDraftUploadedCMOs(), caseDataBefore.getDraftUploadedCMOs())) {
            List<Element<HearingBooking>> hearings = caseData.getHearingDetails();
            List<Element<HearingBooking>> hearingsBefore = caseDataBefore.getHearingDetails();
            hearings.removeAll(hearingsBefore);

            publishEvent(new NewCMOUploaded(caseData, hearings.get(0).getValue()));
        }
    }
}
