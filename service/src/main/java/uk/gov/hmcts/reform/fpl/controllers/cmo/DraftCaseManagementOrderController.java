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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Api
@RestController
@RequestMapping("/callback/upload-cmo")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftCaseManagementOrderController {
    private final CaseManagementOrderService cmoService;
    private final ObjectMapper mapper;

    // TODO: 10/07/2020
    //    • Complete the default scenario for the switch statement (2 or more hearings)
    //    • Next case is there is only 1 hearing
    //    • Handle no possible hearings

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        // populate the list or past hearing dates
        DynamicList pastHearingList = cmoService.getHearingsWithoutCMO(caseData.getPastHearings());

        switch (pastHearingList.getListItems().size()) {
            case 0:
                // handle case of 0 hearings
                // hide list page, show label
                break;
            case 1:
                // handle case of only 1 hearing
                // hide first page and go straight to doc upload
                break;
            default:
                data.put("pastHearingList", pastHearingList);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/update-labels/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleLabelUpdateMidEvent(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        // update judge and hearing labels
        data.putAll(cmoService.getJudgeAndHearingLabels(caseData.getPastHearingList(), caseData.getHearingDetails()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/validate-extension/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleValidationMidEvent(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        DocumentReference doc = mapper.convertValue(data.get("uploadedCaseManagementOrder"), DocumentReference.class);

        List<String> errors = new ArrayList<>();
        if (!doc.hasExtension("pdf")) {
            errors.add("The file must be a pdf");
        }

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

        DynamicList pastHearingList = caseData.getPastHearingList();
        List<Element<HearingBooking>> hearings = caseData.getHearingDetails();
        DocumentReference uploadedCMO = caseData.getUploadedCaseManagementOrder();
        List<Element<CaseManagementOrder>> draftCMOs = caseData.getDraftUploadedCMOs();

        // QUESTION: 10/07/2020 Should these 5 statements all be part of the one method in the service
        // map cmo to hearing
        HearingBooking hearing = cmoService.getSelectedHearing(pastHearingList, hearings);
        CaseManagementOrder draftCMO = cmoService.createDraftCMO(uploadedCMO, hearing);
        Element<CaseManagementOrder> element = element(draftCMO);
        cmoService.mapToHearing(pastHearingList, hearings, element);
        // add to list
        draftCMOs.add(element);


        // update case data
        data.put("draftUploadedCMOs", draftCMOs);
        data.put("hearingDetails", hearings);

        // remove transient fields
        removeTemporaryFields(caseDetails,
            "uploadedCaseManagementOrder",
            "pastHearingList",
            "cmoJudgeInfo",
            "cmoHearingInfo");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/submitted")
    public void handelSubmitted(@RequestBody CallbackRequest request) {
        // send notification
    }
}
