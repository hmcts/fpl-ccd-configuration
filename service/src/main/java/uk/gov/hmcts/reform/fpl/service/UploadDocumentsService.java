package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentMetaData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;

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

    public Map<String, Object> updateCaseDocuments(CaseData caseDataBefore,
                                                   CaseData caseData) {
        List<Element<DocumentSocialWorkOther>> listOfOtherDocs = setUpdatedByAndDateAndTimeOnDocuments(
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
            caseData.getSocialWorkEvidenceTemplateDocument(),
                caseDataBefore.getSocialWorkEvidenceTemplateDocument());
        Document thresholdDocument = setUpdatedByAndDateAndTimeOnDocuments(
            caseData.getThresholdDocument(), caseDataBefore.getThresholdDocument());
        Document checklistDocument = setUpdatedByAndDateAndTimeOnDocuments(
            caseData.getChecklistDocument(), caseDataBefore.getChecklistDocument());
        Document courtBundleDocument = setUpdatedByAndDateAndTimeOnDocuments(
            caseData.getCourtBundle(), caseDataBefore.getCourtBundle()
        );

        Map<String, Object> updatedCaseData = new HashMap<>();

        updatedCaseData.put("documents_socialWorkOther", listOfOtherDocs);
        updatedCaseData.put("documents_socialWorkChronology_document", socialWorkChronologyDocument);
        updatedCaseData.put("documents_socialWorkStatement_document", socialWorkStatementDocument);
        updatedCaseData.put("documents_socialWorkAssessment_document", socialWorkAssessmentDocument);
        updatedCaseData.put("documents_socialWorkCarePlan_document", socialWorkCarePlanDocument);
        updatedCaseData.put("documents_socialWorkEvidenceTemplate_document", socialWorkEvidenceTemplateDocument);
        updatedCaseData.put("documents_threshold_document", thresholdDocument);
        updatedCaseData.put("documents_checklist_document", checklistDocument);
        updatedCaseData.put("courtBundle", courtBundleDocument);

        return updatedCaseData;
    }

    public <T extends DocumentMetaData> List<Element<T>> setUpdatedByAndDateAndTimeOnDocuments(
        List<Element<T>> currentDocuments,
        List<Element<T>> previousDocuments) {

        Predicate<Element<T>> doesNotContainInOldDocs = doc -> previousDocuments != null && !previousDocuments.contains(doc);

        currentDocuments.stream()
            .filter(doesNotContainInOldDocs)
            .forEach(doc -> findElement(doc.getId(), previousDocuments)
                .ifPresent(e -> {
                    if (!e.getValue().getTypeOfDocument().equals(doc.getValue().getTypeOfDocument())) {
                        setUpdatedByAndDateTime(doc);
                    }
                })
            );

        currentDocuments.stream()
            .filter(doc -> doc.getValue().getDateTimeUploaded() == null)
            .forEach(this::setUpdatedByAndDateTime);

        return currentDocuments;
    }

    public Document setUpdatedByAndDateAndTimeOnDocuments(Document currentDoc, Document oldDoc) {

        if (currentDoc == null) {
            return null;
        }

        if ((oldDoc != null && !currentDoc.getTypeOfDocument().equals(oldDoc.getTypeOfDocument()))
            || currentDoc.getDateTimeUploaded() == null) {
            return buildDocument(currentDoc);
        }
        return null;
    }

    private Document buildDocument(Document document) {
        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();

        return document.toBuilder()
            .dateTimeUploaded(time.now())
            .uploadedBy(uploadedBy)
            .build();
    }

    private <T extends DocumentMetaData> void setUpdatedByAndDateTime(Element<T> doc) {
        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();
        doc.getValue().setDateTimeUploaded(time.now());
        doc.getValue().setUploadedBy(uploadedBy);
    }
}
