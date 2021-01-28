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
import uk.gov.hmcts.reform.fpl.handlers.DocumentListService;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ApplicationDocumentsService;
import uk.gov.hmcts.reform.fpl.service.DocumentsValidatorService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentsService;
import uk.gov.hmcts.reform.fpl.service.document.UploadDocumentsMigrationService;

import java.util.List;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/upload-documents")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadDocumentsController extends CallbackController {
    private final DocumentsValidatorService documentsValidatorService;
    private final UploadDocumentsService uploadDocumentsService;
    private final ApplicationDocumentsService applicationDocumentsService;
    private final FeatureToggleService featureToggleService;
    private final UploadDocumentsMigrationService uploadDocumentsMigrationService;
    private final DocumentListService documentListService;

    //Delete after toggle on (no mid-event for new event)
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

        if (featureToggleService.isApplicationDocumentsEventEnabled()) {
            CaseData caseDataBefore = getCaseDataBefore(callbackrequest);
            caseDetails.getData().putAll(applicationDocumentsService.updateApplicationDocuments(
                caseData.getApplicationDocuments(),
                caseDataBefore.getApplicationDocuments()));

            caseDetails.getData().put("documentsList", documentListService.getDocumentsList(caseData));
        } else {
            // New document event is not enabled so move old collection to new
            Map<String, Object> data = caseDetails.getData();
            data.putAll(uploadDocumentsMigrationService.transformFromOldCaseData(caseData));
        }


        return respond(caseDetails);
    }
}

