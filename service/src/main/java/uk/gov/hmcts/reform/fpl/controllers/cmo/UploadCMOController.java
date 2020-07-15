package uk.gov.hmcts.reform.fpl.controllers.cmo;

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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.CaseManagementOrderService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Api
@RestController
@RequestMapping("/callback/upload-cmo")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UploadCMOController {
    private final Time time;
    private final CaseManagementOrderService cmoService;
    private final ObjectMapper mapper;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.putAll(cmoService.getInitialPageData(caseData.getPastHearings()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/update-labels/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleLabelUpdateMidEvent(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        // update judge and hearing labels
        List<Element<HearingBooking>> hearings = cmoService.getHearingsWithoutCMO(caseData.getHearingDetails());
        UUID selectedHearing = cmoService.getSelectedHearingId(caseData.getPastHearingSelector());
        data.putAll(cmoService.getJudgeAndHearingLabels(selectedHearing, hearings));

        reconstructDynamicList(data, hearings, caseData.getPastHearingSelector());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/validate-extension/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleValidationMidEvent(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        List<String> errors = new ArrayList<>();
        if (!caseData.getUploadedCaseManagementOrder().hasExtension(".pdf")) {
            errors.add("The file must be a PDF");
        }

        // reconstruct dynamic list
        List<Element<HearingBooking>> hearings = cmoService.getHearingsWithoutCMO(caseData.getHearingDetails());
        reconstructDynamicList(data, hearings, caseData.getPastHearingSelector());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .errors(errors)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        Object pastHearingSelector = caseData.getPastHearingSelector();
        List<Element<HearingBooking>> hearings = cmoService.getHearingsWithoutCMO(caseData.getHearingDetails());
        DocumentReference uploadedCMO = caseData.getUploadedCaseManagementOrder();
        List<Element<CaseManagementOrder>> draftCMOs = caseData.getDraftUploadedCMOs();

        // QUESTION: 10/07/2020 Should these statements all be part of the one method in the service
        UUID selectedHearingId = cmoService.getSelectedHearingId(pastHearingSelector);
        HearingBooking hearing = cmoService.getSelectedHearing(selectedHearingId, hearings);
        CaseManagementOrder draftCMO = CaseManagementOrder.createDraft(uploadedCMO, hearing, time.now().toLocalDate());
        Element<CaseManagementOrder> element = element(draftCMO);
        cmoService.mapToHearing(selectedHearingId, hearings, element);
        draftCMOs.add(element);


        // update case data
        data.put("draftUploadedCMOs", draftCMOs);
        data.put("hearingDetails", hearings);

        // remove transient fields
        removeTemporaryFields(caseDetails,
            "uploadedCaseManagementOrder",
            "pastHearingSelector",
            "cmoJudgeInfo",
            "cmoHearingInfo",
            "pastHearingsLabel");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/submitted")
    public void handelSubmitted(@RequestBody CallbackRequest request) {
        // send notification
    }

    private void reconstructDynamicList(Map<String, Object> data, List<Element<HearingBooking>> hearings,
                                        Object oldList) {
        DynamicList pastHearingList;

        if (oldList instanceof DynamicList) {
            pastHearingList = (DynamicList) oldList;
        } else {
            UUID selectedHearing = cmoService.getSelectedHearingId(oldList);
            pastHearingList = cmoService.buildDynamicList(hearings, selectedHearing);
        }

        data.put("pastHearingSelector", pastHearingList);
    }
}
