package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("unchecked")
public class PopulateCaseService {
    private static final String FIXTURE_FILE_TEMPLATE = "e2e/fixtures/%s.json";

    private final ObjectMapper mapper;
    private final Time time;
    private final UploadDocumentService uploadDocumentService;

    public Map<String, Object> getFileData(String filename) throws JsonProcessingException {
        String filePath = String.format(FIXTURE_FILE_TEMPLATE, filename);
        String jsonContent = ResourceReader.readString(filePath);

        return mapper.readValue(jsonContent, new TypeReference<>() {});
    }

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

    public State getNewState(String filename) {
        switch (filename) {
            case "gatekeeping":
            case "gatekeepingNoHearingDetails":
                return State.GATEKEEPING;
            case "mandatorySubmissionFields":
            case "mandatoryWithMultipleChildren":
                return State.SUBMITTED;
            case "standardDirectionOrder":
                return State.PREPARE_FOR_HEARING;
            default:
                throw new IllegalArgumentException("Provided filename is not supported");
        }
    }

    public Map<String, Object> getUpdatedSDOData(Map<String, Object> data) {
        var standardDirectionOrder = new HashMap<>((Map<String, Object>) data.get("standardDirectionOrder"));
        standardDirectionOrder.put("orderDoc", uploadMockFile("mockSDO.pdf"));

        return standardDirectionOrder;
    }

    private DocumentReference uploadMockFile(String filename) {
        Document document = uploadDocumentService.uploadPDF(new byte[]{}, filename);

        return DocumentReference.buildFromDocument(document);
    }
}
