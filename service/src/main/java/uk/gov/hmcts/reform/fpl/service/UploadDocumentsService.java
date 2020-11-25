package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentMetaData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UploadDocumentsService {

    private final Time time;
    private final DocumentUploadHelper documentUploadHelper;

    public Map<String, Object> updateCaseDocuments(CaseData caseData, CaseData caseDataBefore) {
        List<Element<DocumentSocialWorkOther>> otherSocialWorkDocuments = setUpdatedByAndDateAndTimeOnDocuments(
                caseData.getOtherSocialWorkDocuments(), caseDataBefore.getOtherSocialWorkDocuments());

        Document socialWorkChronologyDocument = setUpdatedByAndDateAndTimeOnDocuments(
            caseData.getSocialWorkChronologyDocument(), caseDataBefore.getSocialWorkChronologyDocument());

        Document socialWorkStatementDocument = setUpdatedByAndDateAndTimeOnDocuments(
            caseData.getSocialWorkStatementDocument(), caseDataBefore.getSocialWorkStatementDocument());

        Document socialWorkAssessmentDocument = setUpdatedByAndDateAndTimeOnDocuments(
            caseData.getSocialWorkAssessmentDocument(), caseDataBefore.getSocialWorkAssessmentDocument());

        Document socialWorkCarePlanDocument = setUpdatedByAndDateAndTimeOnDocuments(
            caseData.getSocialWorkCarePlanDocument(), caseDataBefore.getSocialWorkCarePlanDocument());

        Document socialWorkEvidenceTemplateDocument = setUpdatedByAndDateAndTimeOnDocuments(
            caseData.getSocialWorkEvidenceTemplateDocument(), caseDataBefore.getSocialWorkEvidenceTemplateDocument());

        Document thresholdDocument = setUpdatedByAndDateAndTimeOnDocuments(
            caseData.getThresholdDocument(), caseDataBefore.getThresholdDocument());

        Document checklistDocument = setUpdatedByAndDateAndTimeOnDocuments(
            caseData.getChecklistDocument(), caseDataBefore.getChecklistDocument());

        Map<String, Object> updatedCaseData = new HashMap<>();

        updatedCaseData.put("documents_socialWorkOther", otherSocialWorkDocuments);
        updatedCaseData.put("documents_socialWorkChronology_document", socialWorkChronologyDocument);
        updatedCaseData.put("documents_socialWorkStatement_document", socialWorkStatementDocument);
        updatedCaseData.put("documents_socialWorkAssessment_document", socialWorkAssessmentDocument);
        updatedCaseData.put("documents_socialWorkCarePlan_document", socialWorkCarePlanDocument);
        updatedCaseData.put("documents_socialWorkEvidenceTemplate_document", socialWorkEvidenceTemplateDocument);
        updatedCaseData.put("documents_threshold_document", thresholdDocument);
        updatedCaseData.put("documents_checklist_document", checklistDocument);

        return updatedCaseData;
    }

    public <T extends DocumentMetaData> List<Element<T>> setUpdatedByAndDateAndTimeOnDocuments(
        List<Element<T>> currentDocuments,
        List<Element<T>> previousDocuments) {

        Predicate<Element<T>> doesNotContainInOldDocs =
            doc -> previousDocuments != null && !previousDocuments.contains(doc);

        currentDocuments.stream()
            .filter(doesNotContainInOldDocs)
            .forEach(doc -> findElement(doc.getId(), previousDocuments)
                .ifPresent(e -> {
                    if (!e.getValue().getTypeOfDocument().equals(doc.getValue().getTypeOfDocument())) {
                        setUpdatedByAndDateTime(doc.getValue());
                    }
                })
            );

        currentDocuments.stream()
            .filter(doc -> doc.getValue().getDateTimeUploaded() == null)
            .forEach(document -> setUpdatedByAndDateTime(document.getValue()));

        return currentDocuments;
    }

    public <T extends DocumentMetaData> T setUpdatedByAndDateAndTimeOnDocuments(T currentDocuments,
                                                                                T previousDocuments) {
        if (currentDocuments == null || currentDocuments.getTypeOfDocument() == null) {
            return null;
        }

        if ((previousDocuments != null
            && !currentDocuments.getTypeOfDocument().equals(previousDocuments.getTypeOfDocument()))
            || currentDocuments.getDateTimeUploaded() == null) {
            return setUpdatedByAndDateTime(currentDocuments);
        }
        return null;
    }

    private <T extends DocumentMetaData> T setUpdatedByAndDateTime(T document) {
        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();

        document.setDateTimeUploaded(time.now());
        document.setUploadedBy(uploadedBy);
        return document;
    }
}
