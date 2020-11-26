package uk.gov.hmcts.reform.fpl.controllers;

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
import uk.gov.hmcts.reform.fpl.service.ApplicationDocumentsService;
import uk.gov.hmcts.reform.fpl.service.DocumentsValidatorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentsService;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Api
@RestController
@RequestMapping("/callback/upload-documents")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadDocumentsController extends CallbackController {
    private final DocumentsValidatorService documentsValidatorService;
    private final UploadDocumentsService uploadDocumentsService;
    private final ApplicationDocumentsService applicationDocumentsService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        caseDetails.getData().remove("showMetaFields");

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = documentsValidatorService.validateDocuments(caseData);

        if (errors.isEmpty()) {
            CaseData caseDataBefore = getCaseDataBefore(callbackrequest);
            caseDetails.getData().putAll(uploadDocumentsService.updateCaseDocuments(caseData, caseDataBefore));
        }

        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        CaseData caseDataBefore = getCaseDataBefore(callbackrequest);
        caseDetails.getData().putAll(applicationDocumentsService.updateCaseDocuments(caseData.getDocuments(),
            caseDataBefore.getDocuments()));

        caseDetails.getData().put("showMetaFields", YES);

        return respond(caseDetails);
    }
}
