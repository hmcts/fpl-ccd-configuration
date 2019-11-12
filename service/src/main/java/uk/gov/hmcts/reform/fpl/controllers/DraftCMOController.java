package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.DirectionHelperService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;

@Api
@RestController
@RequestMapping("/callback/draft-cmo")
public class DraftCMOController {
    private final ObjectMapper mapper;
    private final DraftCMOService draftCMOService;
    private final DirectionHelperService directionHelperService;

    @Autowired
    public DraftCMOController(ObjectMapper mapper,
                              DraftCMOService draftCMOService,
                              DirectionHelperService directionHelperService) {
        this.mapper = mapper;
        this.draftCMOService = draftCMOService;
        this.directionHelperService = directionHelperService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        Map<String, Object> caseDataMap = caseDetails.getData();
        CaseData caseData = mapper.convertValue(caseDataMap, CaseData.class);

        // Resetting allParties - could be pre-populated via SDO
        caseDataMap.remove("allParties");

        if (!isNull(caseData.getCaseManagementOrder())) {
            Map<String, List<Element<Direction>>> directions = directionHelperService.sortDirectionsByAssignee(
                caseData.getCaseManagementOrder().getDirections());
            directions.forEach(caseDataMap::put);
        }

        List<Element<HearingBooking>> hearingDetails = caseData.getHearingDetails();

        DynamicList hearingDatesDynamic = draftCMOService.buildDynamicListFromHearingDetails(hearingDetails);
        Object cmoObject = caseDataMap.get("caseManagementOrder");

        if (cmoObject != null) {
            Order cmo = mapper.convertValue(cmoObject, Order.class);
            String hearingDate = ObjectUtils.isEmpty(cmo) ? "" : cmo.getHearingDate();
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
            .caseManagementOrder(CaseManagementOrder.builder().build())
            .build();

        CaseManagementOrder order = updated.getCaseManagementOrder().toBuilder()
            .hearingDate(list.getValue().getLabel())
            .hearingDateId(list.getValue().getCode())
            .directions(setDirectionAssignee(caseData.getAllParties(), ALL_PARTIES))
            .build();

        caseDetails.getData().remove("cmoHearingDateList");
        caseDetails.getData().remove("allParties");
        caseDetails.getData().put("caseManagementOrder", order);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private List<Element<Direction>> setDirectionAssignee(List<Element<Direction>> directions,
                                                            DirectionAssignee assignee) {
        if (!isNull(directions)) {
            return directions.stream()
                .map(element -> Element.<Direction>builder()
                    .id(element.getId())
                    .value(element.getValue().toBuilder()
                        .assignee(assignee).build())
                    .build())
                .collect(toList());
        } else {
            return emptyList();
        }
    }
}
