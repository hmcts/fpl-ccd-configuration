package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UploadApplicationDocumentsService {

    private final Time time;
    private final DocumentUploadHelper documentUploadHelper;

    public Map<String, Object> updateCaseDocuments(CaseData caseData, CaseData caseDataBefore) {
        List<Element<ApplicationDocument>> otherSocialWorkDocuments = setUpdatedByAndDateAndTimeOnDocuments(
            caseData.getDocuments(), caseDataBefore.getDocuments());

        Map<String, Object> updatedCaseData = new HashMap<>();

        updatedCaseData.put("documents", otherSocialWorkDocuments);

        return updatedCaseData;
    }

    public List<Element<ApplicationDocument>> setUpdatedByAndDateAndTimeOnDocuments(
        List<Element<ApplicationDocument>> currentDocuments,
        List<Element<ApplicationDocument>> previousDocuments) {
        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();

        if (isNull(previousDocuments) && currentDocuments.size() > 0) {
            // this is for very first scenario

            currentDocuments.get(currentDocuments.size() - 1).getValue().setDateTimeUploaded(time.now());
            currentDocuments.get(currentDocuments.size() - 1).getValue().setUploadedBy(uploadedBy);
            return currentDocuments;
        } else {

            //An old document which have not been changed
            List<Element<ApplicationDocument>> currentDocs = currentDocuments.stream()
                .map(currentDoc -> {
                    Optional<Element<ApplicationDocument>> previousDoc = getMetaDataBasedOnID(currentDoc.getId(), previousDocuments);

                    if(previousDoc.isPresent()) {
                        Element<ApplicationDocument> oldDocument = previousDoc.get();


                        if (oldDocument.getId().equals(currentDoc.getId())) {
                            //id's are same so element modified potentially
                            if (oldDocument.getValue().getDocument().equals(currentDoc.getValue().getDocument())) {
                                // docs wasn't modified so keep persist as old author
                                currentDoc.getValue().setDateTimeUploaded(oldDocument.getValue().getDateTimeUploaded());
                                currentDoc.getValue().setUploadedBy(oldDocument.getValue().getUploadedBy());
                            } else {
                                //ids same but has been modified so update author
                                currentDoc.getValue().setDateTimeUploaded(LocalDateTime.now());
                                currentDoc.getValue().setUploadedBy(uploadedBy);
                            }
                        }
                    } else {
                        //previous doc doesn't exist therefore new one has been added
                        currentDoc.getValue().setDateTimeUploaded(LocalDateTime.now());
                        currentDoc.getValue().setUploadedBy(uploadedBy);
                    }

                    return currentDoc;
                }).collect(Collectors.toList());


            return currentDocs;


        }
    }

    private Optional<Element<ApplicationDocument>> getMetaDataBasedOnID(UUID documentID, List<Element<ApplicationDocument>> previousDocuments) {
        return Optional.ofNullable(previousDocuments.stream().filter((previousDocument) -> previousDocument.getId()
            .equals(documentID)).findAny().orElse(null));
    }
}
