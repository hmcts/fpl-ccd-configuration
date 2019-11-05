package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
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
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

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
        Object cmoObject = caseDataMap.get("caseManagementOrder");

        if (cmoObject != null) {
            Order cmo = mapper.convertValue(cmoObject, Order.class);
            String hearingDate = cmo.getHearingDate();
            if (!isEmpty(hearingDate)) {
                DynamicListElement element = DynamicListElement.builder()
                    .label(hearingDate)
                    .code(hearingDate)
                    .build();

                hearingDatesDynamic.setValue(element);
                if (!hearingDatesDynamic.getListItems().contains(element)) {
                    hearingDatesDynamic.getListItems().add(element);
                }
            }
        }

        caseDataMap.put("cmoHearingDateList", hearingDatesDynamic);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataMap)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        DynamicList list = mapper.convertValue(caseDetails.getData().get("cmoHearingDateList"), DynamicList.class);

        CaseData updated = caseData.toBuilder()
            .caseManagementOrder(Order.builder().build())
            .build();

        Order order = updated.getCaseManagementOrder()
            .toBuilder().hearingDate(list.getValue().getCode())
            .build();

        caseDetails.getData().remove("cmoHearingDateList");
        caseDetails.getData().put("caseManagementOrder", order);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();

    }
}
