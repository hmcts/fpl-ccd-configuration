package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("unchecked")
public class PopulateCaseService {

    private final Time time;

    public Map<String, Object> getTimeBasedAndDocumentData() {
        var mockDocument = Map.of("documentStatus", "Attached", "typeOfDocument", uploadMockFile("mockFile.txt"));

        return Map.of(
            "dateAndTimeSubmitted", time.now().toString(),
            "dateSubmitted", time.now().toLocalDate().toString(),
            "submittedForm", uploadMockFile("mockSubmittedApplication.pdf"),
            "documents_checklist_document", mockDocument,
            "documents_threshold_document", mockDocument,
            "documents_socialWorkCarePlan_document", mockDocument,
            "documents_socialWorkAssessment_document", mockDocument,
            "documents_socialWorkEvidenceTemplate_document", mockDocument
        );
    }

    public Map<String, Object> getUpdatedSDOData(Map<String, Object> data) {
        var standardDirectionOrder = new HashMap<>((Map<String, Object>) data.get("standardDirectionOrder"));
        standardDirectionOrder.put("orderDoc", uploadMockFile("mockSDO.pdf"));

        return standardDirectionOrder;
    }

    private DocumentReference uploadMockFile(String filename) {
        return DocumentReference.builder()
            .filename(filename)
            .url("http://fakeUrl")
            .binaryUrl("http://fakeBinaryUrl")
            .build();
    }
}
