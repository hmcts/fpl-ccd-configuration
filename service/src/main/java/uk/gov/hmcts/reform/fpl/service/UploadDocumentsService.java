package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentMetaData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UploadDocumentsService {

    private final Time time;
    private final DocumentUploadHelper documentUploadHelper;

    public <T extends DocumentMetaData> List<Element<T>> setUpdatedByAndDateAndTimeForDocuments(
        List<Element<T>> listOfCurrentDocs,
        List<Element<T>> listOfOldDocs) {

        listOfCurrentDocs.stream()
            .filter(doc -> listOfOldDocs != null && !listOfOldDocs.contains(doc))
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
