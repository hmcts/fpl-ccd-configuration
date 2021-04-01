package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.common.DocumentMetaData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

import java.util.List;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UploadDocumentsService {

    private final Time time;
    private final DocumentUploadHelper documentUploadHelper;

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
