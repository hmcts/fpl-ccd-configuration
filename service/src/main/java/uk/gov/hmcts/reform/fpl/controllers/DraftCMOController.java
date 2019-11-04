package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.*;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.*;
import java.util.List;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/draft-cmo")
public class DraftCMOController {
    private final ObjectMapper mapper;
    private final DraftCMOService draftCMOService;

    @Autowired
    public DraftCMOController(ObjectMapper mapper, DraftCMOService draftCMOService) {
        this.mapper = mapper;
        this.draftCMOService = draftCMOService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        Map<String, Object> caseDataMap = caseDetails.getData();
        CaseData caseData = mapper.convertValue(caseDataMap, CaseData.class);

        List<Element<HearingBooking>> hearingDetails = caseData.getHearingDetails();

        DynamicList hearingDatesDynamic = draftCMOService.makeHearingDateList(hearingDetails);
        Object list = caseDataMap.get("cmoHearingDateList");
        if (list != null) {
            // Old list will have the previous selected value
            DynamicList oldList = mapper.convertValue(list, DynamicList.class);
            hearingDatesDynamic = oldList.merge(hearingDatesDynamic);
        }
        caseDataMap.put("cmoHearingDateList", hearingDatesDynamic);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataMap)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        DynamicList list = mapper.convertValue(caseData.get("cmoHearingDateList"), DynamicList.class);
        list.prepareForStorage();
        caseData.put("cmoHearingDateList", list);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData)
            .build();

    }
}
