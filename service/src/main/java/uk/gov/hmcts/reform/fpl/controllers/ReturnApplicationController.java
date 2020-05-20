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
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Api
@RestController
@RequestMapping("/callback/return-application")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReturnApplicationController {
    public static final String RETURN_APPLICATION = "returnApplication";
    private final ObjectMapper mapper;

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

        caseDetails.getData().put(RETURN_APPLICATION, buildReturnApplication(caseData, documentReference));
        caseDetails.getData().put("submittedForm", null);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private ReturnApplication buildReturnApplication(CaseData caseData,
                                                     DocumentReference documentReference) {
        documentReference.setFilename(buildReturnedFileName(documentReference.getFilename()));

        return ReturnApplication.builder()
            .note(caseData.getReturnApplication().getNote())
            .reason(caseData.getReturnApplication().getReason())
            .document(documentReference)
            .returnedDate(formatLocalDateToString(now(), "dd MMM YYYY"))
            .submittedDate(formatLocalDateToString(caseData.getDateSubmitted(), "dd MMM YYYY"))
            .build();
    }

    private String buildReturnedFileName(String fileName) {
        String documentName = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.'));

        return documentName + "_returned" + extension;
    }
}
