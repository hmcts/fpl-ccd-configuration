package uk.gov.hmcts.reform.fpl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.UploadResponse;

import java.io.IOException;

public class ResourceLoader {

    private static final ObjectMapper mapper = new ObjectMapper();

    private ResourceLoader() {
        // NO-OP
    }

    public static UploadResponse successfulDocumentManagementUploadResponse() throws IOException {
        String response = ResourceReader.readString("document-management-store-api/upload-success.json");
        return mapper.readValue(response, UploadResponse.class);
    }

    public static UploadResponse unsuccessfulDocumentManagementUploadResponse() throws IOException {
        String response = ResourceReader.readString("document-management-store-api/upload-failure.json");
        return mapper.readValue(response, UploadResponse.class);
    }

    public static CaseDetails emptyCaseDetails() throws IOException {
        String response = ResourceReader.readString("ccd-case-data-api/empty-case-details.json");
        return mapper.readValue(response, CaseDetails.class);
    }

    public static CaseDetails populatedCaseDetails() throws IOException {
        String response = ResourceReader.readString("ccd-case-data-api/populated-case-details.json");
        return mapper.readValue(response, CaseDetails.class);
    }
}
