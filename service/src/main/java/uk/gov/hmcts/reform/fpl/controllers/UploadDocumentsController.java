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
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ApplicationDocumentsService;
import uk.gov.hmcts.reform.fpl.service.DocumentsValidatorService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentsService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.CARE_PLAN;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.CHECKLIST_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SOCIAL_WORK_CHRONOLOGY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SOCIAL_WORK_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SWET;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.THRESHOLD;
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
            addDocumentsToApplicationDocumentCollection(caseData, caseDetails);
        }

        return respond(caseDetails);
    }

    private void addDocumentsToApplicationDocumentCollection(CaseData caseData, CaseDetails caseDetails) {
        List<Element<ApplicationDocument>> applicationDocuments = new ArrayList<>();

        Map<Document, ApplicationDocumentType> documentsToProcess = new LinkedHashMap<>();
        documentsToProcess.put(caseData.getSocialWorkChronologyDocument(), SOCIAL_WORK_CHRONOLOGY);
        documentsToProcess.put(caseData.getSocialWorkStatementDocument(), SOCIAL_WORK_STATEMENT);
        documentsToProcess.put(caseData.getSocialWorkAssessmentDocument(), SOCIAL_WORK_STATEMENT);
        documentsToProcess.put(caseData.getSocialWorkCarePlanDocument(), CARE_PLAN);
        documentsToProcess.put(caseData.getSocialWorkEvidenceTemplateDocument(), SWET);
        documentsToProcess.put(caseData.getThresholdDocument(), THRESHOLD);
        documentsToProcess.put(caseData.getChecklistDocument(), CHECKLIST_DOCUMENT);

        //this maps to other
        //need to loop through these
        List<Element<DocumentSocialWorkOther>> otherDocuments = caseData.getOtherSocialWorkDocuments();

        for (Map.Entry<Document, ApplicationDocumentType> document : documentsToProcess.entrySet()) {
            Document applicationDocument = document.getKey();
            ApplicationDocumentType documentType = document.getValue();

            if(!isNull(applicationDocument.getDocumentStatus()) || !isNull(applicationDocument.getTypeOfDocument())) {
                //cater for if status is to follow
                ApplicationDocument updatedDocument = convertOldDocumentToNewApplicationDocument(applicationDocument, documentType);
                applicationDocuments.add(element(updatedDocument));
            }
        }

        caseDetails.getData().put("applicationDocuments", applicationDocuments);
    }

    private ApplicationDocument convertOldDocumentToNewApplicationDocument(Document document, ApplicationDocumentType documentType) {

        ApplicationDocument applicationDocument = ApplicationDocument.builder()
            .document(document.getTypeOfDocument())
            .dateTimeUploaded(document.getDateTimeUploaded())
            .uploadedBy(document.getUploadedBy())
            .documentType(documentType)
            .includedInSWET(null)
            .build();

        return applicationDocument;
    }
}

