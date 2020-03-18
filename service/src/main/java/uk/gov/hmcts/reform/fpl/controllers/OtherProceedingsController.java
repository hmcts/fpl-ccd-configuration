package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Api
@RestController
@RequestMapping("callback/enter-other-proceedings")
public class OtherProceedingsController {
    private static final String ERROR_MESSAGE = "You must say if there are any other proceedings relevant to this case";

    @SuppressWarnings("unchecked")
    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        Map<String, Object> proceedingData = (Map<String, Object>) caseDetails.getData().get("proceeding");
        List<String> validationErrors = new ArrayList<>();
        String onGoingProceeding = (String) proceedingData.get("onGoingProceeding");

        if (onGoingProceeding == null || "".equals(onGoingProceeding)) {
            validationErrors.add(ERROR_MESSAGE);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validationErrors)
            .build();
    }
}
