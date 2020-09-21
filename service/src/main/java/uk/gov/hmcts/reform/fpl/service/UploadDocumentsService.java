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

    public Map<String, Object> updateCaseDetailsWithDocuments(CaseData caseDataBefore,
                                                               CaseData caseData) {
        List<Element<DocumentSocialWorkOther>> listOfOtherDocs = setUpdatedByAndDateAndTimeForDocuments(
                caseData.getOtherSocialWorkDocuments(), caseDataBefore.getOtherSocialWorkDocuments());

        Document socialWorkChronologyDocument = setUpdatedByAndDateAndTimeForDocuments(
            caseData.getSocialWorkChronologyDocument(), caseDataBefore.getSocialWorkChronologyDocument());
        Document socialWorkStatementDocument = setUpdatedByAndDateAndTimeForDocuments(
            caseData.getSocialWorkStatementDocument(), caseDataBefore.getSocialWorkStatementDocument());
        Document socialWorkAssessmentDocument = setUpdatedByAndDateAndTimeForDocuments(
            caseData.getSocialWorkAssessmentDocument(), caseDataBefore.getSocialWorkAssessmentDocument());
        Document socialWorkCarePlanDocument = setUpdatedByAndDateAndTimeForDocuments(
            caseData.getSocialWorkCarePlanDocument(), caseDataBefore.getSocialWorkCarePlanDocument());
        Document socialWorkEvidenceTemplateDocument = setUpdatedByAndDateAndTimeForDocuments(
            caseData.getSocialWorkEvidenceTemplateDocument(),
                caseDataBefore.getSocialWorkEvidenceTemplateDocument());
        Document thresholdDocument = setUpdatedByAndDateAndTimeForDocuments(
            caseData.getThresholdDocument(), caseDataBefore.getThresholdDocument());
        Document checklistDocument = setUpdatedByAndDateAndTimeForDocuments(
            caseData.getChecklistDocument(), caseDataBefore.getChecklistDocument());
        Document courtBundleDocument = setUpdatedByAndDateAndTimeForDocuments(
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

    public <T extends DocumentMetaData> List<Element<T>> setUpdatedByAndDateAndTimeForDocuments(
        List<Element<T>> listOfCurrentDocs,
        List<Element<T>> listOfOldDocs) {

        Predicate<Element<T>> containsInListOfOldDocs = doc -> listOfOldDocs != null && !listOfOldDocs.contains(doc);

        listOfCurrentDocs.stream()
            .filter(containsInListOfOldDocs)
            .forEach(doc -> findElement(doc.getId(), listOfOldDocs)
                .ifPresent(e -> {
                    if (!e.getValue().getTypeOfDocument().equals(doc.getValue().getTypeOfDocument())) {
                        setUpdatedByAndDateTime(doc);
                    }
                })
            );

        listOfCurrentDocs.stream()
            .filter(doc -> doc.getValue().getDateTimeUploaded() == null)
            .forEach(this::setUpdatedByAndDateTime);

        return listOfCurrentDocs;
    }

    public Document setUpdatedByAndDateAndTimeForDocuments(Document currentDoc, Document oldDoc) {

        if (oldDoc != null && !currentDoc.getTypeOfDocument().equals(oldDoc.getTypeOfDocument())) {
            return buildDocument(currentDoc);
        } else if (currentDoc.getDateTimeUploaded() == null) {
            return buildDocument(currentDoc);
        }
        return null;
    }

    private Document buildDocument(Document currentDoc) {
        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();

        return currentDoc.toBuilder()
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
