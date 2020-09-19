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
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DocumentsValidatorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Api
@RestController
@RequestMapping("/callback/upload-documents")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadDocumentsController extends CallbackController {
    private final DocumentsValidatorService documentsValidatorService;
    private final UploadDocumentsService uploadDocumentsService;

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseData caseDataBefore = getCaseDataBefore(callbackrequest);
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = documentsValidatorService.validateDocuments(caseData);

        if (errors.isEmpty()) {
            caseDetails.getData().putAll(updateCaseDetailsWithDocuments(caseDataBefore, caseData));
        }

        return respond(caseDetails, documentsValidatorService.validateDocuments(caseData));
    }

    private Map<String, Object> updateCaseDetailsWithDocuments(CaseData caseDataBefore,
                                                               CaseData caseData) {
        List<Element<DocumentSocialWorkOther>> listOfOtherDocs =
            uploadDocumentsService.setUpdatedByAndDateAndTimeForDocuments(
                caseData.getOtherSocialWorkDocuments(), caseDataBefore.getOtherSocialWorkDocuments());

        BiFunction<Document, Document, Document> setUpdatedByAndDateForDocument =
            uploadDocumentsService::setUpdatedByAndDateAndTimeForDocuments;

        Document socialWorkChronologyDocument = setUpdatedByAndDateForDocument
            .apply(caseData.getSocialWorkChronologyDocument(), caseDataBefore.getSocialWorkChronologyDocument());
        Document socialWorkStatementDocument = setUpdatedByAndDateForDocument
            .apply(caseData.getSocialWorkStatementDocument(), caseDataBefore.getSocialWorkStatementDocument());
        Document socialWorkAssessmentDocument = setUpdatedByAndDateForDocument
            .apply(caseData.getSocialWorkAssessmentDocument(), caseDataBefore.getSocialWorkAssessmentDocument());
        Document socialWorkCarePlanDocument = setUpdatedByAndDateForDocument
            .apply(caseData.getSocialWorkCarePlanDocument(), caseDataBefore.getSocialWorkCarePlanDocument());

        Document socialWorkEvidenceTemplateDocument = setUpdatedByAndDateForDocument
            .apply(caseData.getSocialWorkEvidenceTemplateDocument(),
                caseDataBefore.getSocialWorkEvidenceTemplateDocument());

        Document thresholdDocument = setUpdatedByAndDateForDocument
            .apply(caseData.getThresholdDocument(), caseDataBefore.getThresholdDocument());

        Document checklistDocument = setUpdatedByAndDateForDocument
            .apply(caseData.getChecklistDocument(), caseDataBefore.getChecklistDocument());

        Map<String, Object> updatedCaseData = new HashMap<>();

        updatedCaseData.put("documents_socialWorkOther", listOfOtherDocs);
        updatedCaseData.put("documents_socialWorkChronology_document", socialWorkChronologyDocument);
        updatedCaseData.put("documents_socialWorkStatement_document", socialWorkStatementDocument);
        updatedCaseData.put("documents_socialWorkAssessment_document", socialWorkAssessmentDocument);
        updatedCaseData.put("documents_socialWorkCarePlan_document", socialWorkCarePlanDocument);
        updatedCaseData.put("documents_socialWorkEvidenceTemplate_document",
            socialWorkEvidenceTemplateDocument);
        updatedCaseData.put("documents_threshold_document", thresholdDocument);
        updatedCaseData.put("documents_checklist_document", checklistDocument);

        return updatedCaseData;
    }
}
