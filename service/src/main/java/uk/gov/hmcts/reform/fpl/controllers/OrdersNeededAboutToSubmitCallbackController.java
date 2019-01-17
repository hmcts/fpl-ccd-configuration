package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Api
@RestController
@RequestMapping("/callback/orders-needed")
public class OrdersNeededAboutToSubmitCallbackController {

    @PostMapping("/about-to-submit")
    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(
        @RequestBody CallbackRequest callbackrequest) {
        CaseDetails newCaseDetails = callbackrequest.getCaseDetails();
        CaseDetails oldCaseDetails = callbackrequest.getCaseDetailsBefore();

        Map<String, Object> newData = newCaseDetails.getData();
        Optional<List<String>> newOrderType = Optional.ofNullable((Map<String, Object>) newData.get("orders"))
            .map(orders -> (List<String>) orders.get("orderType"));

        Map<String, Object> oldData = oldCaseDetails.getData();
        Optional<List<String>> oldOrderType = Optional.ofNullable((Map<String, Object>) oldData.get("orders"))
            .map(orders -> (List<String>) orders.get("orderType"));

        if (newOrderType.toString().contains("EMERGENCY_PROTECTION_ORDER")) {
            newData.put("EPO_REASONING_SHOW", new String[]{"SHOW_FIELD"});
        }

        if (oldOrderType.toString().contains("EMERGENCY_PROTECTION_ORDER") && !newOrderType.toString().contains("EMERGENCY_PROTECTION_ORDER")) {
            newData.remove("groundsForEPO");
            newData.remove("EPO_REASONING_SHOW");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(newData)
            .build();
    }
}
