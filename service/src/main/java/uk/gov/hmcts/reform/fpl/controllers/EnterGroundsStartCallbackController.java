package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/enter-grounds")
public class EnterGroundsStartCallbackController {

    @Autowired
    public EnterGroundsStartCallbackController() {
    }

    @PostMapping("/about-to-start")
    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(
        @RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        Map<String, Object> ordersAndDirections = (Map<String, Object>) caseDetails.getData().get("orders");

        if (ordersAndDirections.get("orderType").toString().equals("[EMERGENCY_PROTECTION_ORDER]" )) {
            Map<String, Object> data = caseDetails.getData();
            data.put("grounds", ImmutableMap.<String, String>builder()
                .put("EPO_REASONING_SHOW", "SHOW_FIELD")
                .build());

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(data)
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }
}
