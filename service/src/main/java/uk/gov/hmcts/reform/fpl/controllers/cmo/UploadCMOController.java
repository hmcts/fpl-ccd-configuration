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
import uk.gov.hmcts.reform.fpl.events.cmo.CMOReadyToSealEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.cmo.UploadCMOService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder.from;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Api
@RestController
@RequestMapping("/callback/upload-cmo")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UploadCMOController {
    private static final String[] TRANSIENT_FIELDS = {
        "uploadedCaseManagementOrder", "pastHearingList", "cmoJudgeInfo", "cmoHearingInfo", "numHearings",
        "singleHearingsWithCMOs", "multiHearingsWithCMOs", "showHearingsSingleTextArea", "showHearingsMultiTextArea"
    };

    private final Time time;
    private final UploadCMOService cmoService;
    private final ObjectMapper mapper;
    private final ApplicationEventPublisher publisher;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.putAll(cmoService.getInitialPageData(caseData.getPastHearings(), caseData.getDraftUploadedCMOs()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleLabelMidEvent(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        // update judge and hearing labels
        Object dynamicList = caseData.getPastHearingList();
        List<Element<HearingBooking>> hearings = cmoService.getHearingsWithoutCMO(caseData.getPastHearings(),
            caseData.getDraftUploadedCMOs());
        UUID selectedHearing = cmoService.getSelectedHearingId(dynamicList, hearings);
        data.putAll(cmoService.getJudgeAndHearingDetails(selectedHearing, hearings));

        if (!(dynamicList instanceof DynamicList)) {
            // reconstruct dynamic list
            data.put("pastHearingList", cmoService.buildDynamicList(hearings, selectedHearing));
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
        List<Element<CaseManagementOrder>> draftCMOs = caseData.getDraftUploadedCMOs();
        List<Element<HearingBooking>> hearingsWithoutCMO = cmoService.getHearingsWithoutCMO(
            caseData.getPastHearings(), draftCMOs
        );

        if (!hearingsWithoutCMO.isEmpty()) {
            List<Element<HearingBooking>> hearings = caseData.getHearingDetails();
            UUID selectedHearingId = cmoService.getSelectedHearingId(caseData.getPastHearingList(),
                hearingsWithoutCMO);
            HearingBooking hearing = cmoService.getSelectedHearing(selectedHearingId, hearingsWithoutCMO);

            Element<CaseManagementOrder> element = element(from(caseData.getUploadedCaseManagementOrder(),
                hearing, time.now().toLocalDate()));

            UUID uuid = cmoService.mapToHearing(selectedHearingId, hearings, element);

            if (uuid != null) {
                // overwrite old draft CMO
                int index = -1;
                for (int i = 0; i < draftCMOs.size(); i++) {
                    if (draftCMOs.get(i).getId().equals(uuid)) {
                        index = i;
                        break;
                    }
                }

                draftCMOs.set(index, element);
            } else {
                draftCMOs.add(element);
            }

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

        List<Element<CaseManagementOrder>> draftCMOs = caseData.getDraftUploadedCMOs();
        List<Element<CaseManagementOrder>> draftCMOsBefore = caseDataBefore.getDraftUploadedCMOs();
        draftCMOs.removeAll(draftCMOsBefore);

        if (draftCMOs.size() == 1) {
            List<Element<HearingBooking>> hearings = caseData.getHearingDetails();
            List<Element<HearingBooking>> hearingsBefore = caseDataBefore.getHearingDetails();
            hearings.removeAll(hearingsBefore);

            publisher.publishEvent(new CMOReadyToSealEvent(request, hearings.get(0).getValue()));
        }
    }

}
