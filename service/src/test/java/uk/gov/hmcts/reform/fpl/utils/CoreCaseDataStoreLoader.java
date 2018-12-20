package uk.gov.hmcts.reform.fpl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.io.IOException;
import java.util.Map;

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

    public static StartEventResponse successfulStartEventResponse() throws IOException {
        String response = ResourceReader.readString("core-case-data-store-api/responses/start-event-success.json");
        return mapper.readValue(response, StartEventResponse.class);
    }
}
