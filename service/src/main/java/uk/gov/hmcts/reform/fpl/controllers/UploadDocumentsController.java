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

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

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
        List<String> errors = new ArrayList<>();

        List<Element<ApplicationDocument>> currentDocuments = caseData.getApplicationDocuments();
        List<Element<ApplicationDocument>> previousDocuments = caseDataBefore.getApplicationDocuments();

        if (currentDocuments == null || previousDocuments == null) {
            errors.add("We encountered a problem storing the data, please try again and re-enter all information. " +
                "Apologies for the inconvenience.");
        }

        if (isNotEmpty(errors)) {
            return respond(caseDetails, errors);
        }

        caseDetails.getData().putAll(applicationDocumentsService.updateApplicationDocuments(
            currentDocuments, previousDocuments));

        caseDetails.getData().putAll(documentListService.getDocumentView(getCaseData(caseDetails)));
        return respond(caseDetails);
    }
}

