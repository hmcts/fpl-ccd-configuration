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
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ApplicationDocumentsService;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;

import java.util.List;
import java.util.Optional;

@Api
@RestController
@RequestMapping("/callback/upload-documents")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadDocumentsController extends CallbackController {
    private final ApplicationDocumentsService applicationDocumentsService;
    private final DocumentListService documentListService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        CaseData caseDataBefore = getCaseDataBefore(callbackrequest);

        List<Element<ApplicationDocument>> currentDocuments = Optional.ofNullable(
            caseData.getApplicationDocuments())
            .orElseThrow(() -> new IllegalStateException(
                "Unexpected null current application documents. " + caseData));

        List<Element<ApplicationDocument>> previousDocuments = Optional.ofNullable(
            caseDataBefore.getApplicationDocuments())
            .orElseThrow(() -> new IllegalStateException(
                "Unexpected null previous application documents. " + caseDataBefore));;

        caseDetails.getData().putAll(applicationDocumentsService.updateApplicationDocuments(
            currentDocuments, previousDocuments));

        caseDetails.getData().putAll(documentListService.getDocumentView(getCaseData(caseDetails)));
        return respond(caseDetails);
    }
}

