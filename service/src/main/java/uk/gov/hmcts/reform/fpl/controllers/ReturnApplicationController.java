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
import uk.gov.hmcts.reform.fpl.model.returnapplication.ReturnedDocumentBundle;

import java.time.LocalDate;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Api
@RestController
@RequestMapping("/callback/return-application")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReturnApplicationController {
    public static final String RETURN_APPLICATION = "returnApplication";
    public static final String RETURNED_DOCUMENT_BUNDLE = "returnedDocumentBundle";
    private final ObjectMapper mapper;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        caseDetails.getData().put(RETURN_APPLICATION, null);
        caseDetails.getData().put(RETURNED_DOCUMENT_BUNDLE, null);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        LocalDate dateSubmitted = caseData.getDateSubmitted();
        DocumentReference documentReference = mapper.convertValue(caseDetails.getData().get("submittedForm"),
            DocumentReference.class);

        ReturnedDocumentBundle returnedDocumentBundle = buildReturnedDocumentBundle(documentReference, dateSubmitted);

        caseDetails.getData().put(RETURNED_DOCUMENT_BUNDLE, returnedDocumentBundle);
        caseDetails.getData().put("submittedForm", null);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private ReturnedDocumentBundle buildReturnedDocumentBundle(DocumentReference documentReference,
                                                               LocalDate dateSubmitted) {
        documentReference.setFilename(buildReturnedFileName(documentReference.getFilename()));

        return ReturnedDocumentBundle.builder()
            .document(documentReference)
            .returnedDate(formatLocalDateToString(now(), "dd MMM YYYY"))
            .submittedDate(formatLocalDateToString(dateSubmitted, "dd MMM YYYY"))
            .build();
    }

    private String buildReturnedFileName(String fileName) {
        String documentName = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.'));

        return documentName + "_returned" + extension;
    }
}
