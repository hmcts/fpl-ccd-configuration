package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.assertj.core.util.Lists;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/upload-c2")
public class C2UploadController {

    @PostMapping("/about-to-submit")
    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();

        List<Object> uploadC2 = (List<Object>) data.computeIfAbsent("uploadC2", (notUsed) -> Lists.newArrayList());

        Map<String, Object> newElement = new HashMap<>();
        newElement.put("value", data.get("tempC2"));
        uploadC2.add(newElement);
        data.put("tempC2", null);

        return AboutToStartOrSubmitCallbackResponse.builder().data(data).build();
    }
}
