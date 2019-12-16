package uk.gov.hmcts.reform.fpl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;

public class CoreCaseDataStoreLoader {

    private static final ObjectMapper mapper = new ObjectMapper();

    private CoreCaseDataStoreLoader() {
        // NO-OP
    }

    public static CaseDetails emptyCaseDetails() throws IOException {
        String response = ResourceReader.readString("core-case-data-store-api/empty-case-details.json");
        return mapper.readValue(response, CaseDetails.class);
    }

    public static CaseDetails populatedCaseDetails() throws IOException {
        String response = ResourceReader.readString("core-case-data-store-api/populated-case-details.json");
        return mapper.readValue(response, CaseDetails.class);
    }

    public static CallbackRequest callbackRequest() throws IOException {
        String response = ResourceReader.readString("core-case-data-store-api/callback-request.json");
        return mapper.readValue(response, CallbackRequest.class);
    }

    public static CallbackRequest careOrderRequest() throws IOException {
        String response = ResourceReader.readString("core-case-data-store-api/care-order.json");
        return mapper.readValue(response, CallbackRequest.class);
    }
}
