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
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApplicationDocumentsService {

    private final Time time;
    private final DocumentUploadHelper documentUploadHelper;

    public Map<String, Object> updateCaseDocuments(List<Element<ApplicationDocument>> currentDocuments,
                                                   List<Element<ApplicationDocument>> previousDocuments) {
        List<Element<ApplicationDocument>> updatedDocuments = setUpdatedByAndDateAndTimeOnDocuments(
             currentDocuments, previousDocuments);

        Map<String, Object> updatedCaseData = new HashMap<>();

        updatedCaseData.put("documents", updatedDocuments);

        return updatedCaseData;
    }

    public List<Element<ApplicationDocument>> setUpdatedByAndDateAndTimeOnDocuments(
        List<Element<ApplicationDocument>> currentDocuments,
        List<Element<ApplicationDocument>> previousDocuments) {

        if (isNull(previousDocuments) && currentDocuments.size() > 0) {
            currentDocuments.stream().forEach(this::setUpdatedByAndDateAndTimeOnDocumentToCurrent);
            return currentDocuments;
        } else {
            List<Element<ApplicationDocument>> documents = currentDocuments.stream()
                .map(document -> {
                    Optional<Element<ApplicationDocument>> documentBefore = getDocumentBeforeFromID(document.getId(),
                        previousDocuments);

                    if (documentBefore.isPresent()) {
                        Element<ApplicationDocument> oldDocument = documentBefore.get();

                        if (oldDocument.getId().equals(document.getId())) {
                            if (oldDocument.getValue().getDocument().equals(document.getValue().getDocument())) {
                                // Document wasn't modified so persist old values
                                document.getValue().setDateTimeUploaded(oldDocument.getValue().getDateTimeUploaded());
                                document.getValue().setUploadedBy(oldDocument.getValue().getUploadedBy());
                            } else {
                                // Document was modified so updated
                                setUpdatedByAndDateAndTimeOnDocumentToCurrent(document);
                            }
                        }
                    } else {
                        // New document was added
                        setUpdatedByAndDateAndTimeOnDocumentToCurrent(document);
                    }
                    return document;
                }).collect(Collectors.toList());

            return documents;
        }
    }

    private Optional<Element<ApplicationDocument>> getDocumentBeforeFromID(UUID documentID,
                                                                           List<Element<ApplicationDocument>> previousDocuments) {
        return Optional.ofNullable(previousDocuments.stream()
            .filter((previousDocument)
            -> previousDocument.getId()
            .equals(documentID))
            .findAny()
            .orElse(null));
    }

    private Element<ApplicationDocument> setUpdatedByAndDateAndTimeOnDocumentToCurrent(Element<ApplicationDocument> document) {
        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();

        document.getValue().setDateTimeUploaded(time.now());
        document.getValue().setUploadedBy(uploadedBy);
        return document;
    }
}
