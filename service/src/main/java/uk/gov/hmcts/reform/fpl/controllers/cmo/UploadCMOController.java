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
        Object dynamicList = caseData.getPastHearingSelector();
        List<Element<HearingBooking>> hearings = cmoService.getHearingsWithoutCMO(caseData.getHearingDetails());
        UUID selectedHearing = cmoService.getSelectedHearingId(dynamicList);
        data.putAll(cmoService.getJudgeAndHearingLabels(selectedHearing, hearings));

        if (!(dynamicList instanceof DynamicList)) {
            // reconstruct dynamic list
            data.put("pastHearingSelector", cmoService.buildDynamicList(hearings, selectedHearing));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        List<Element<HearingBooking>> hearings = cmoService.getHearingsWithoutCMO(caseData.getHearingDetails());
        if (hearings.size() != 0) {
            Object pastHearingSelector = caseData.getPastHearingSelector();
            DocumentReference uploadedCMO = caseData.getUploadedCaseManagementOrder();
            List<Element<CaseManagementOrder>> draftCMOs = caseData.getDraftUploadedCMOs();

            // QUESTION: 10/07/2020 Should these statements all be part of the one method in the service
            UUID selectedHearingId = cmoService.getSelectedHearingId(pastHearingSelector);
            HearingBooking hearing = cmoService.getSelectedHearing(selectedHearingId, hearings);
            CaseManagementOrder draftCMO = CaseManagementOrder.createDraft(uploadedCMO,
                hearing,
                time.now().toLocalDate());
            Element<CaseManagementOrder> element = element(draftCMO);
            cmoService.mapToHearing(selectedHearingId, hearings, element);
            draftCMOs.add(element);


            // update case data
            data.put("draftUploadedCMOs", draftCMOs);
            data.put("hearingDetails", hearings);
        }
        // remove transient fields
        removeTemporaryFields(caseDetails, CaseManagementOrderService.TRANSIENT_FIELDS);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/submitted")
    public void handelSubmitted(@RequestBody CallbackRequest request) {
        CaseDetails caseDetailsBefore = request.getCaseDetailsBefore();
        Map<String, Object> dataBefore = caseDetailsBefore.getData();
        CaseData caseDataBefore = mapper.convertValue(dataBefore, CaseData.class);

        if (cmoService.getHearingsWithoutCMO(caseDataBefore.getPastHearings()).size() != 0) {
            // send notification
        }
    }

}
