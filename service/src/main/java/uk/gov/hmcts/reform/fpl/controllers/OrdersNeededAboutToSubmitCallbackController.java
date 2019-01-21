package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
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
        String epo = "EMERGENCY_PROTECTION_ORDER";
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();

        Optional<List<String>> orderType = Optional.ofNullable((Map<String, Object>) data.get("orders"))
            .map(orders -> (List<String>) orders.get("orderType"));

        if (orderType.isPresent()) {
            orderType.ifPresent(orderTypes -> {
                if (orderTypes.contains(epo)) {
                    data.put("EPO_REASONING_SHOW", ImmutableList.of("SHOW_FIELD"));

                } else if (data.containsKey("EPO_REASONING_SHOW")) {
                    data.remove("groundsForEPO");
                    data.remove("EPO_REASONING_SHOW");
                }
            });

        } else {
            data.remove("groundsForEPO");
            data.remove("EPO_REASONING_SHOW");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }
}
