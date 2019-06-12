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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Api
@RestController
@RequestMapping("/callback/enter-social-work-other")
public class SocialWorkOtherSubmissionController {

    @SuppressWarnings("unchecked")
    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        List<Map<String, Object>> socialWorkOtherData = (List<Map<String, Object>>) caseDetails
            .getData().get("documents_socialWorkOther");
        List<String> validationErrors = new ArrayList<String>();

        if (!socialWorkOtherData.isEmpty()) {
            AtomicInteger i = new AtomicInteger(1);

            socialWorkOtherData.forEach(document -> {
                Map<String, Object> documentValue = (Map<String, Object>) document.get("value");
                Optional documentTitle = Optional.ofNullable(documentValue.get("documentTitle"));

                if (documentTitle.isEmpty() || "".equals(documentValue.get("documentTitle"))) {
                    validationErrors.add(String.format("You must give additional document %s a name.", i.get()));
                }

                i.getAndIncrement();
            });
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validationErrors)
            .build();
    }
}
