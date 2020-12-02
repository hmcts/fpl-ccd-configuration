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
import java.util.List;

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
            processCaseDataAndExtractOldDocuments(caseData, caseDetails);
        }

        return respond(caseDetails);
    }

    private void processCaseDataAndExtractOldDocuments(CaseData caseData, CaseDetails caseDetails) {
        // Extract All Old documents
        //this maps to social work chronology
        Document socialWorkChronologyDocument = caseData.getSocialWorkChronologyDocument();
        //this could map to social work statement or genogram
        Document socialWorkStatementDocument = caseData.getSocialWorkStatementDocument();
        //what does document 3 Social work assessment map to
        //***
        //this maps to care plan
        Document socialWorkCarePlanDocument = caseData.getSocialWorkCarePlanDocument();
        //this maps to swet
        Document socialWorkEvidenceTemplateDocument = caseData.getSocialWorkEvidenceTemplateDocument();
        //this maps to threshold
        Document thresholdDocument = caseData.getThresholdDocument();
        //this maps to checklist
        Document checklistDocument = caseData.getChecklistDocument();
        //this maps to other
        //need to loop through these
        List<Element<DocumentSocialWorkOther>> otherDocuments = caseData.getOtherSocialWorkDocuments();


        if(!isNull(socialWorkChronologyDocument.getDocumentStatus()) || !isNull(socialWorkChronologyDocument.getTypeOfDocument())){
            //currently null not working here
            //currently can't add multiple documents
            //if status is set as to follow and no document attached should we copy nothing over
            List<Element<ApplicationDocument>> updatedApplicationDocuments = convertOldDocumentsToNewApplicationDocuments(socialWorkChronologyDocument, SOCIAL_WORK_CHRONOLOGY);
            caseDetails.getData().put("applicationDocuments", updatedApplicationDocuments);
        }

        if(!isNull(socialWorkStatementDocument.getDocumentStatus()) || !isNull(socialWorkStatementDocument.getTypeOfDocument())){
            List<Element<ApplicationDocument>> updatedApplicationDocuments = convertOldDocumentsToNewApplicationDocuments(socialWorkStatementDocument, SOCIAL_WORK_STATEMENT);
            caseDetails.getData().put("applicationDocuments", updatedApplicationDocuments);
        }

        if(!isNull(socialWorkCarePlanDocument.getDocumentStatus()) || !isNull(socialWorkCarePlanDocument.getTypeOfDocument())){
            List<Element<ApplicationDocument>> updatedApplicationDocuments = convertOldDocumentsToNewApplicationDocuments(socialWorkCarePlanDocument, CARE_PLAN);
            caseDetails.getData().put("applicationDocuments", updatedApplicationDocuments);
        }

        if(!isNull(socialWorkEvidenceTemplateDocument.getDocumentStatus()) || !isNull(socialWorkEvidenceTemplateDocument.getTypeOfDocument())){
            List<Element<ApplicationDocument>> updatedApplicationDocuments = convertOldDocumentsToNewApplicationDocuments(socialWorkEvidenceTemplateDocument, SWET);
            caseDetails.getData().put("applicationDocuments", updatedApplicationDocuments);
        }

        if(!isNull(thresholdDocument.getDocumentStatus()) || !isNull(thresholdDocument.getTypeOfDocument())){
            List<Element<ApplicationDocument>> updatedApplicationDocuments = convertOldDocumentsToNewApplicationDocuments(thresholdDocument, THRESHOLD);
            caseDetails.getData().put("applicationDocuments", updatedApplicationDocuments);
        }

        if(!isNull(checklistDocument.getDocumentStatus()) || !isNull(checklistDocument.getTypeOfDocument())){
            List<Element<ApplicationDocument>> updatedApplicationDocuments = convertOldDocumentsToNewApplicationDocuments(checklistDocument, CHECKLIST_DOCUMENT);
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

        //is included in swet fine as null or does it need to be empty string
        //cater for if it's an 'other document' then set document name


        applicationDocuments.add(element(applicationDocument));
        return applicationDocuments;
    }
}

