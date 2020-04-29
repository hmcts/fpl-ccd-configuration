package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.util.List;
import java.util.Map;


@Api
@RestController
@RequestMapping("/callback/populate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("unchecked")
public class PopulateCaseController {
    private static final String FIXTURE_FILE_TEMPLATE = "e2e/fixtures/%s.json";

    private final ObjectMapper mapper;
    private final UploadDocumentService uploadDocumentService;
    private final Time time;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();

        String filename = data.get("caseDataFilename").toString();
        try {
            data.putAll(readFileData(filename));
        } catch (Exception e) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(data)
                .errors(List.of(String.format("Could not read file %s", filename)))
                .build();
        }

        var mockDocument = Map.of("documentStatus", "Attached", "typeOfDocument", uploadMockFile("mockFile.txt"));
        data.putAll(Map.of(
            "dateAndTimeSubmitted", time.now().toString(),
            "dateSubmitted", time.now().toLocalDate().toString(),
            "submittedForm", uploadMockFile("mockSubmittedApplication.pdf"),
            "documents_checklist_document", mockDocument,
            "documents_threshold_document", mockDocument,
            "documents_socialWorkCarePlan_document", mockDocument,
            "documents_socialWorkAssessment_document", mockDocument,
            "documents_socialWorkEvidenceTemplate_document", mockDocument,
            "state", getNewState(filename).getValue()
        ));


        if (filename.equals("sdo")) {
            Map<String, Object> sdo = (Map<String, Object>) data.get("standardDirectionOrder");
            sdo.put("orderDoc", uploadMockFile("mockSDO.pdf"));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private Map<String, Object> readFileData(String filename) throws Exception {
        String filePath = String.format(FIXTURE_FILE_TEMPLATE, filename);
        String jsonContent = ResourceReader.readString(filePath);

        return mapper.readValue(jsonContent, new TypeReference<>() {});
    }

    private DocumentReference uploadMockFile(String filename) {
        Document document = uploadDocumentService.uploadPDF(new byte[]{}, filename);

        return DocumentReference.buildFromDocument(document);
    }

    private State getNewState(String filename) {
        if (filename.equals("gatekeeping")) {
            return State.GATEKEEPING;
        }
        if (filename.equals("sdo")) {
            return State.PREPARE_FOR_HEARING;
        }

        return State.SUBMITTED;
    }
}
