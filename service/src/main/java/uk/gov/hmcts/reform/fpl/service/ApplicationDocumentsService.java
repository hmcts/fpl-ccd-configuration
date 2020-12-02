package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.jsonwebtoken.lang.Collections.isEmpty;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.CARE_PLAN;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.CHECKLIST_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SOCIAL_WORK_CHRONOLOGY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SOCIAL_WORK_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SWET;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.THRESHOLD;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApplicationDocumentsService {

    private final Time time;
    private final DocumentUploadHelper documentUploadHelper;

    public Map<String, Object> updateApplicationDocuments(List<Element<ApplicationDocument>> currentDocuments,
                                                          List<Element<ApplicationDocument>> previousDocuments) {
        List<Element<ApplicationDocument>> updatedDocuments = setUpdatedByAndDateAndTimeOnDocuments(
             currentDocuments, previousDocuments);

        Map<String, Object> data = new HashMap<>();

        data.put("applicationDocuments", updatedDocuments);

        return data;
    }

    public List<Element<ApplicationDocument>> convertOldDocumentsToApplicationDocumentCollection(CaseData caseData) {
        List<Element<ApplicationDocument>> applicationDocuments = new ArrayList<>();

        Map<Document, ApplicationDocumentType> documentsToProcess = new LinkedHashMap<>();
        documentsToProcess.put(caseData.getSocialWorkChronologyDocument(), SOCIAL_WORK_CHRONOLOGY);
        documentsToProcess.put(caseData.getSocialWorkStatementDocument(), SOCIAL_WORK_STATEMENT);
        documentsToProcess.put(caseData.getSocialWorkAssessmentDocument(), SOCIAL_WORK_STATEMENT);
        documentsToProcess.put(caseData.getSocialWorkCarePlanDocument(), CARE_PLAN);
        documentsToProcess.put(caseData.getSocialWorkEvidenceTemplateDocument(), SWET);
        documentsToProcess.put(caseData.getThresholdDocument(), THRESHOLD);
        documentsToProcess.put(caseData.getChecklistDocument(), CHECKLIST_DOCUMENT);

        for (Map.Entry<Document, ApplicationDocumentType> document : documentsToProcess.entrySet()) {
            Document applicationDocument = document.getKey();
            ApplicationDocumentType documentType = document.getValue();

            if(!isNull(applicationDocument.getDocumentStatus()) || !isNull(applicationDocument.getTypeOfDocument())) {
                //cater for if status is to follow
                ApplicationDocument updatedDocument = convertOldDocumentToApplicationDocument(applicationDocument, documentType);
                applicationDocuments.add(element(updatedDocument));
            }
        }

        List<Element<DocumentSocialWorkOther>> otherDocuments = caseData.getOtherSocialWorkDocuments();
        if(!otherDocuments.isEmpty())
        {
            handleOtherDocuments(otherDocuments, applicationDocuments);
        }

        return applicationDocuments;
    }

    private List<Element<ApplicationDocument>> setUpdatedByAndDateAndTimeOnDocuments(
        List<Element<ApplicationDocument>> currentDocuments,
        List<Element<ApplicationDocument>> previousDocuments) {

        if (isEmpty(previousDocuments) && !isEmpty(currentDocuments)) {
            currentDocuments.forEach(this::setUpdatedByAndDateAndTimeOnDocumentToCurrent);
            return currentDocuments;
        }

        return currentDocuments.stream()
            .map(document -> {
                Optional<Element<ApplicationDocument>> documentBefore = findElement(document.getId(),
                    previousDocuments);

                if (documentBefore.isPresent()) {
                    Element<ApplicationDocument> oldDocument = documentBefore.get();
                    handleExistingDocuments(document, oldDocument);

                } else {
                    // New document was added
                    setUpdatedByAndDateAndTimeOnDocumentToCurrent(document);
                }
                return document;
            }).collect(Collectors.toList());
    }

    private void handleExistingDocuments(Element<ApplicationDocument> document,
                                         Element<ApplicationDocument> documentBefore) {
        if (documentBefore.getId().equals(document.getId())) {
            if (documentBefore.getValue().getDocument().equals(document.getValue().getDocument())) {
                // Document wasn't modified so persist old values
                document.getValue().setDateTimeUploaded(documentBefore.getValue().getDateTimeUploaded());
                document.getValue().setUploadedBy(documentBefore.getValue().getUploadedBy());
            } else {
                // Document was modified so update
                setUpdatedByAndDateAndTimeOnDocumentToCurrent(document);
            }
        }
    }

    private void setUpdatedByAndDateAndTimeOnDocumentToCurrent(
        Element<ApplicationDocument> document) {
        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();

        document.getValue().setDateTimeUploaded(time.now());
        document.getValue().setUploadedBy(uploadedBy);
    }

    private void handleOtherDocuments(List<Element<DocumentSocialWorkOther>> otherDocuments,  List<Element<ApplicationDocument>> applicationDocuments) {
        otherDocuments.stream().forEach(document -> {
            if(!isNull(document.getValue().getTypeOfDocument()) || !isNull(document.getValue().getDocumentTitle())) {
                ApplicationDocument updatedDocument = buildApplicationDocumentWithTypeOther(document.getValue(), OTHER);
                applicationDocuments.add(element(updatedDocument));
            }
        });
    }

    private ApplicationDocument convertOldDocumentToApplicationDocument(Document document, ApplicationDocumentType documentType) {

        ApplicationDocument applicationDocument = ApplicationDocument.builder()
            .document(document.getTypeOfDocument())
            .dateTimeUploaded(document.getDateTimeUploaded())
            .uploadedBy(document.getUploadedBy())
            .documentType(documentType)
            .includedInSWET(null)
            .build();

        return applicationDocument;
    }

    private ApplicationDocument buildApplicationDocumentWithTypeOther(DocumentSocialWorkOther document, ApplicationDocumentType documentType) {

        ApplicationDocument applicationDocument = ApplicationDocument.builder()
            .document(document.getTypeOfDocument())
            .dateTimeUploaded(document.getDateTimeUploaded())
            .uploadedBy(document.getUploadedBy())
            .documentType(documentType)
            .documentName(document.getDocumentTitle())
            .build();

        return applicationDocument;
    }
}
