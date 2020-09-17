package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UploadDocumentsService {

    private final Time time;
    private final DocumentUploaderService documentUploaderService;

    public List<Element<DocumentSocialWorkOther>> getOtherSocialWorkDocuments(CaseData caseDataBefore,
                                                                              CaseData caseDataCurrent) {
        List<Element<DocumentSocialWorkOther>> listOfCurrentDocs = caseDataCurrent.getOtherSocialWorkDocuments();
        List<Element<DocumentSocialWorkOther>> listOfOldDocs = caseDataBefore.getOtherSocialWorkDocuments();

        listOfCurrentDocs.stream()
            .filter(doc -> !listOfOldDocs.contains(doc))
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

    private void setUpdatedByAndDateTime(Element<DocumentSocialWorkOther> doc) {
        String uploadedBy = documentUploaderService.getUploadedDocumentUserDetails();
        doc.getValue().setDateTimeUploaded(time.now());
        doc.getValue().setUploadedBy(uploadedBy);
    }
}
