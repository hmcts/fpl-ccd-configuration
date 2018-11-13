package uk.gov.hmcts.reform.fpl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;

import java.io.IOException;

public class ResourceLoader {

    private ResourceLoader() {
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static UploadResponse successfulDocumentManagementUploadResponse() throws IOException {
        String response = new ResourceReader().read("responses/success.json");
        return objectMapper.readValue(response, UploadResponse.class);
    }

    public static UploadResponse unsuccessfulDocumentManagementUploadResponse() throws IOException {
        String response = new ResourceReader().read("responses/failure.json");
        return objectMapper.readValue(response, UploadResponse.class);
    }

    public static CaseDetails emptyCaseDetails() throws IOException {
        String response = new ResourceReader().read("caseDetails/emptyCaseDetails.json");
        return objectMapper.readValue(response, CaseDetails.class);
    }

    public static CaseDetails populatedCaseDetails() throws IOException {
        String response = new ResourceReader().read("caseDetails/populatedCaseDetails.json");
        return objectMapper.readValue(response, CaseDetails.class);
    }

    public static CallbackRequest successfulCallBack() throws IOException {
        String response = new ResourceReader().read("callbackRequest/successfulCallbackRequest.json");
        return objectMapper.readValue(response, CallbackRequest.class);
    }

    public static Document successfulDocumentUpload() throws IOException {
        String response = new ResourceReader().read("responses/successDocument.json");
        return objectMapper.readValue(response, Document.class);
    }

    public static StartEventResponse successfulStartEventResponse() throws IOException {
        String response = new ResourceReader().read("responses/successfulStartEvent.json");
        return objectMapper.readValue(response, StartEventResponse.class);
    }
}
