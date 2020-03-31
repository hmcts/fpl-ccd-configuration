package uk.gov.hmcts.reform.fpl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.io.UncheckedIOException;

public class CoreCaseDataStoreLoader {

    private static final ObjectMapper mapper = new ObjectMapper();

    private CoreCaseDataStoreLoader() {
        // NO-OP
    }

    public static CaseDetails emptyCaseDetails() {
        String response = ResourceReader.readString("core-case-data-store-api/empty-case-details.json");
        return read(response, CaseDetails.class);
    }

    public static CaseDetails populatedCaseDetails() {
        String response = ResourceReader.readString("core-case-data-store-api/populated-case-details.json");
        return read(response, CaseDetails.class);
    }

    public static CallbackRequest callbackRequest() {
        String response = ResourceReader.readString("core-case-data-store-api/callback-request.json");
        return read(response, CallbackRequest.class);
    }

    private static <T> T read(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
