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
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.ReturnApplicationService;

@Api
@RestController
@RequestMapping("/callback/return-application")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReturnApplicationController {
    public static final String RETURN_APPLICATION = "returnApplication";
    private final ObjectMapper mapper;
    private final ReturnApplicationService returnApplicationService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        caseDetails.getData().put(RETURN_APPLICATION, null);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        DocumentReference documentReference = mapper.convertValue(caseDetails.getData().get("submittedForm"),
            DocumentReference.class);

        String updatedFileName = returnApplicationService.appendReturnedToFileName(documentReference.getFilename());
        documentReference.setFilename(updatedFileName);

        caseDetails.getData().put(RETURN_APPLICATION, returnApplicationService.updateReturnApplication(
            caseData.getReturnApplication(), documentReference, caseData.getDateSubmitted()));

        caseDetails.getData().put("submittedForm", null);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
