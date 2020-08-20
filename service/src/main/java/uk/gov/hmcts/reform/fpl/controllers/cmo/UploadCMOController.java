package uk.gov.hmcts.reform.fpl.controllers.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.cmo.NewCMOUploaded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.cmo.UploadCMOService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@Api
@RestController
@RequestMapping("/callback/upload-cmo")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UploadCMOController {
    private static final String[] TRANSIENT_FIELDS = {
        "uploadedCaseManagementOrder", "hearingsWithoutApprovedCMO", "cmoJudgeInfo", "cmoHearingInfo",
        "numHearingsWithoutCMO", "singleHearingWithCMO", "multiHearingsWithCMOs", "showHearingsSingleTextArea",
        "showHearingsMultiTextArea"
    };

    private final UploadCMOService cmoService;
    private final ObjectMapper mapper;
    private final ApplicationEventPublisher publisher;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.putAll(cmoService.getInitialPageData(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleLabelMidEvent(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        // handle document fields being set to null when clicking previous button
        DocumentReference uploadedCMO = caseData.getUploadedCaseManagementOrder();
        if (uploadedCMO != null && uploadedCMO.isEmpty()) {
            data.remove("uploadedCaseManagementOrder");
        }

        // update judge and hearing labels
        data.putAll(cmoService.prepareJudgeAndHearingDetails(caseData, caseData.getHearingsWithoutApprovedCMO()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);
        List<Element<CaseManagementOrder>> draftCMOs = caseData.getDraftUploadedCMOs();
        DocumentReference uploadedCaseManagementOrder = caseData.getUploadedCaseManagementOrder();

        if (uploadedCaseManagementOrder != null) {
            List<Element<HearingBooking>> hearings = caseData.getPastHearings();
            cmoService.updateHearingsAndUnsealedCMOs(caseData, draftCMOs, uploadedCaseManagementOrder,
                caseData.getHearingsWithoutApprovedCMO());
            // update case data
            data.put("draftUploadedCMOs", draftCMOs);
            data.put("hearingDetails", hearings);
        }
        // remove transient fields
        removeTemporaryFields(caseDetails, TRANSIENT_FIELDS);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/submitted")
    public void handelSubmitted(@RequestBody CallbackRequest request) {
        CaseData caseDataBefore = mapper.convertValue(request.getCaseDetailsBefore().getData(), CaseData.class);
        CaseData caseData = mapper.convertValue(request.getCaseDetails().getData(), CaseData.class);

        if (cmoService.isNewCmoUploaded(caseData.getDraftUploadedCMOs(), caseDataBefore.getDraftUploadedCMOs())) {
            List<Element<HearingBooking>> hearings = caseData.getHearingDetails();
            List<Element<HearingBooking>> hearingsBefore = caseDataBefore.getHearingDetails();
            hearings.removeAll(hearingsBefore);

            publisher.publishEvent(new NewCMOUploaded(request, hearings.get(0).getValue()));
        }
    }
}
