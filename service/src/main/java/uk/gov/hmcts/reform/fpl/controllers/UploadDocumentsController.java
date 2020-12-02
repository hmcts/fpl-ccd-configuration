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
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ApplicationDocumentsService;
import uk.gov.hmcts.reform.fpl.service.DocumentsValidatorService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentsService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SOCIAL_WORK_CHRONOLOGY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Api
@RestController
@RequestMapping("/callback/upload-documents")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadDocumentsController extends CallbackController {
    private final DocumentsValidatorService documentsValidatorService;
    private final UploadDocumentsService uploadDocumentsService;
    private final ApplicationDocumentsService applicationDocumentsService;
    private final FeatureToggleService featureToggleService;

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

        if(featureToggleService.isApplicationDocumentsEventEnabled()) {
            CaseData caseDataBefore = getCaseDataBefore(callbackrequest);
            caseDetails.getData().putAll(applicationDocumentsService.updateApplicationDocuments(
                caseData.getApplicationDocuments(),
                caseDataBefore.getApplicationDocuments()));
        } else {
            // New document event is not enabled so move old collection to new
            processCaseDataAndExtractOldDocuments(caseData, caseDetails);
        }

        return respond(caseDetails);
    }

    private void processCaseDataAndExtractOldDocuments(CaseData caseData, CaseDetails caseDetails) {
        // Extract All Old documents
        Document socialWorkChronologyDocument = caseData.getSocialWorkChronologyDocument();

        if (socialWorkChronologyDocument != null) {
            List<Element<ApplicationDocument>> updatedApplicationDocuments = convertOldDocumentsToNewApplicationDocuments(socialWorkChronologyDocument, SOCIAL_WORK_CHRONOLOGY);
            caseDetails.getData().put("applicationDocuments", updatedApplicationDocuments);
        }
    }

    private List<Element<ApplicationDocument>> convertOldDocumentsToNewApplicationDocuments(Document document,
                                                                                            ApplicationDocumentType documentType) {

        List<Element<ApplicationDocument>> applicationDocuments = new ArrayList<>();

        ApplicationDocument applicationDocument = ApplicationDocument.builder()
            .document(document.getTypeOfDocument())
            .dateTimeUploaded(document.getDateTimeUploaded())
            .uploadedBy(document.getUploadedBy())
            .documentType(documentType)
            .includedInSWET(null)
            .build();

        //cater for if it's an 'other document'


        applicationDocuments.add(element(applicationDocument));
        return applicationDocuments;
    }
}

