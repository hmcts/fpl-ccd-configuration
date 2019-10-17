package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/upload-documents")
public class UploadDocumentController {

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse aboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();

        // Check if the court bundle should be visible
        String state = caseDetails.getState();
        String displayCourtBundle = state.equals("Open") ? "No" : "Yes";
        data.put("displayCourtBundle", displayCourtBundle);

        return AboutToStartOrSubmitCallbackResponse.builder().data(data).build();
    }
}
