package uk.gov.hmcts.reform.fpl.controllers;

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
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MigrateCaseController {

    private final ObjectMapper mapper;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if ("SA20C50034".equals(caseData.getFamilyManCaseNumber()) && 1600893083735118L == caseDetails.getId()) {
            data.put("hearingDetails", removeHearingLinkedToCmo(caseData.getHearingDetails(),
                caseData.getSealedCMOs().get(0).getId()));

            data.put("sealedCMOs", removeCaseManagementOrder(caseData.getSealedCMOs()));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private List<Element<CaseManagementOrder>> removeCaseManagementOrder(List<Element<CaseManagementOrder>> orders) {
        orders.remove(0);
        return orders;
    }

    private List<Element<HearingBooking>> removeHearingLinkedToCmo(List<Element<HearingBooking>> hearingBookings,
                                                                   UUID elementId) {
        for (Element<HearingBooking> hearingBooking : hearingBookings) {
            if (elementId.equals(hearingBooking.getValue().getCaseManagementOrderId())) {
                hearingBooking.getValue().setCaseManagementOrderId(null);
            }
        }

        return hearingBookings;
    }
}
