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
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;

@Api
@RestController
@RequestMapping("/callback/upload-documents")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadDocumentsController extends CallbackController {
    private final ApplicationDocumentsService applicationDocumentsService;
    private final DocumentListService documentListService;
    private final FeatureToggleService featureToggleService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        CaseData caseDataBefore = getCaseDataBefore(callbackrequest);

        caseDetails.getData().putAll(applicationDocumentsService.updateApplicationDocuments(
            caseData.getApplicationDocuments(), caseDataBefore.getApplicationDocuments()));

        if (featureToggleService.isFurtherEvidenceDocumentTabEnabled()) {
            caseDetails.getData().put("documentsList", documentListService.getDocumentsList(getCaseData(caseDetails)));
        }
        return respond(caseDetails);
    }
}

