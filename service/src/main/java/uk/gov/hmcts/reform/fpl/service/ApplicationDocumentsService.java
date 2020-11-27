package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.jsonwebtoken.lang.Collections.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApplicationDocumentsService {

    private final Time time;
    private final DocumentUploadHelper documentUploadHelper;

    public Map<String, Object> updateCaseDocuments(List<Element<ApplicationDocument>> currentDocuments,
                                                   List<Element<ApplicationDocument>> previousDocuments) {
        List<Element<ApplicationDocument>> updatedDocuments = setUpdatedByAndDateAndTimeOnDocuments(
             currentDocuments, previousDocuments);

        Map<String, Object> data = new HashMap<>();

        data.put("applicationDocuments", updatedDocuments);

        return data;
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

    private Element<ApplicationDocument> setUpdatedByAndDateAndTimeOnDocumentToCurrent(
        Element<ApplicationDocument> document) {
        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();

        document.getValue().setDateTimeUploaded(time.now());
        document.getValue().setUploadedBy(uploadedBy);
        return document;
    }
}
