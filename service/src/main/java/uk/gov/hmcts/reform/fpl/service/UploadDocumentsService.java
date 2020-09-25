package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UploadDocumentsService {

    private final Time time;
    private final DocumentUploadHelper documentUploadHelper;
    private final ObjectMapper mapper;

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

        CourtBundle courtBundleDocument = setUpdatedByAndDateAndTimeOnDocuments(
            caseData.getCourtBundle(), caseDataBefore.getCourtBundle());

        CaseData updateCaseDataWithDocuments = CaseData.builder()
            .otherSocialWorkDocuments(otherSocialWorkDocuments)
            .socialWorkChronologyDocument(socialWorkChronologyDocument)
            .socialWorkStatementDocument(socialWorkStatementDocument)
            .socialWorkAssessmentDocument(socialWorkAssessmentDocument)
            .socialWorkCarePlanDocument(socialWorkCarePlanDocument)
            .socialWorkEvidenceTemplateDocument(socialWorkEvidenceTemplateDocument)
            .thresholdDocument(thresholdDocument)
            .checklistDocument(checklistDocument)
            .courtBundle(courtBundleDocument)
            .build();

        return mapper.convertValue(updateCaseDataWithDocuments, new TypeReference<>() {});
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
