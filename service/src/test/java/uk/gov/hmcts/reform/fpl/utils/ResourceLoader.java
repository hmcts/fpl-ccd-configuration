package uk.gov.hmcts.reform.fpl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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
}
