package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.BIRTH_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.CARE_PLAN;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.CHECKLIST_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.GENOGRAM;
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
    private final ManageDocumentService manageDocumentService;
    private final UserService userService;

    private Map<String, Object> synchroniseToNewFields(List<Element<ApplicationDocument>> applicationDocuments,
                                                       List<ApplicationDocumentType> applicationDocumentTypes,
                                                       String newFieldName) {
        final List<Element<ManagedDocument>> newDocListLA =
            Optional.ofNullable(applicationDocuments).orElse(new ArrayList<>()).stream()
                .filter(fed -> applicationDocumentTypes.contains(fed.getValue().getDocumentType()))
                .filter(fed -> fed.getValue().isConfidentialDocument())
                .map(fed -> element(fed.getId(), ManagedDocument.builder()
                    .document(fed.getValue().getDocument())
                    .uploaderCaseRoles(fed.getValue().getUploaderCaseRoles())
                    .uploaderType(fed.getValue().getUploaderType())
                    .build()))
                .collect(toList());

        final List<Element<ManagedDocument>> newDocList =
            Optional.ofNullable(applicationDocuments).orElse(new ArrayList<>()).stream()
                .filter(fed -> applicationDocumentTypes.contains(fed.getValue().getDocumentType()))
                .filter(fed -> !fed.getValue().isConfidentialDocument())
                .map(fed -> element(fed.getId(), ManagedDocument.builder()
                    .document(fed.getValue().getDocument())
                    .uploaderCaseRoles(fed.getValue().getUploaderCaseRoles())
                    .uploaderType(fed.getValue().getUploaderType())
                    .build()))
                .collect(toList());

        Map<String, Object> ret = new HashMap<>();
        ret.put(newFieldName, newDocList);
        ret.put(newFieldName + "LA", newDocListLA);
        return ret;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> synchroniseToNewFields(List<Element<ApplicationDocument>> applicationDocuments) {
        Map<String, Object> ret = new HashMap<>();
        ret.putAll(synchroniseToNewFields(applicationDocuments, List.of(SWET, SOCIAL_WORK_CHRONOLOGY,
                SOCIAL_WORK_STATEMENT, GENOGRAM, CHECKLIST_DOCUMENT, BIRTH_CERTIFICATE, OTHER),
            "documentsFiledOnIssueList"));
        ret.putAll(synchroniseToNewFields(applicationDocuments, List.of(CARE_PLAN), "carePlanList"));
        ret.putAll(synchroniseToNewFields(applicationDocuments, List.of(THRESHOLD), "thresholdList"));
        return ret;
    }

    public Map<String, Object> updateApplicationDocuments(CaseData caseData,
                                                          List<Element<ApplicationDocument>> currentDocuments,
                                                          List<Element<ApplicationDocument>> previousDocuments) {
        return updateApplicationDocuments(caseData, currentDocuments, previousDocuments,
            "temporaryApplicationDocuments");
    }

    private Map<String, Object> updateApplicationDocuments(CaseData caseData,
                                                          List<Element<ApplicationDocument>> currentDocuments,
                                                          List<Element<ApplicationDocument>> previousDocuments,
                                                          String populatedField) {
        List<Element<ApplicationDocument>> updatedDocuments = setUpdatedByAndDateAndTimeOnDocuments(caseData,
            currentDocuments, previousDocuments);

        Map<String, Object> data = new HashMap<>();

        data.put(populatedField, updatedDocuments);
        data.putAll(synchroniseToNewFields(new ArrayList<>(updatedDocuments)));

        return data;
    }


    private List<Element<ApplicationDocument>> setUpdatedByAndDateAndTimeOnDocuments(CaseData caseData,
        List<Element<ApplicationDocument>> currentDocuments,
        List<Element<ApplicationDocument>> previousDocuments) {

        if (isEmpty(previousDocuments) && !isEmpty(currentDocuments)) {
            currentDocuments.forEach(d ->  setUpdatedByAndDateAndTimeOnDocumentToCurrent(caseData, d));
            return currentDocuments;
        }

        return currentDocuments.stream()
            .map(document -> {
                Optional<Element<ApplicationDocument>> documentBefore = findElement(document.getId(),
                    previousDocuments);

                // in the old flow, we allowed other documents with just title and no file
                if (documentBefore.map(doc -> doc.getValue().hasDocument()).orElse(false)) {
                    Element<ApplicationDocument> oldDocument = documentBefore.get();
                    handleExistingDocuments(caseData, document, oldDocument);

                } else {
                    // New document was added
                    setUpdatedByAndDateAndTimeOnDocumentToCurrent(caseData, document);
                }
                return document;
            }).collect(Collectors.toList());
    }

    private void handleExistingDocuments(CaseData caseData,
                                         Element<ApplicationDocument> document,
                                         Element<ApplicationDocument> documentBefore) {
        if (documentBefore.getId().equals(document.getId())) {
            if (documentBefore.getValue().getDocument().equals(document.getValue().getDocument())) {
                // Document wasn't modified so persist old values
                document.getValue().setDateTimeUploaded(documentBefore.getValue().getDateTimeUploaded());
                document.getValue().setUploadedBy(documentBefore.getValue().getUploadedBy());
                document.getValue().setUploaderCaseRoles(new ArrayList<>(userService.getCaseRoles(caseData.getId())));
                document.getValue().setUploaderType(manageDocumentService.getUploaderType(caseData));
            } else {
                // Document was modified so update
                setUpdatedByAndDateAndTimeOnDocumentToCurrent(caseData, document);
            }
        }
    }

    private void setUpdatedByAndDateAndTimeOnDocumentToCurrent(CaseData caseData,
        Element<ApplicationDocument> document) {
        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();

        document.getValue().setDateTimeUploaded(time.now());
        document.getValue().setUploadedBy(uploadedBy);
        document.getValue().setUploaderCaseRoles(new ArrayList<>(userService.getCaseRoles(caseData.getId())));
        document.getValue().setUploaderType(manageDocumentService.getUploaderType(caseData));
    }
}
