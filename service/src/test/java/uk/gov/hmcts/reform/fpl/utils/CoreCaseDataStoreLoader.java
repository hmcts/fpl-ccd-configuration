package uk.gov.hmcts.reform.fpl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.StringSubstitutor;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import static java.util.Collections.emptyMap;

public class CoreCaseDataStoreLoader {

    private static final ObjectMapper mapper = new ObjectMapper();

    private CoreCaseDataStoreLoader() {
        // NO-OP
    }

    public static CaseDetails emptyCaseDetails() {
        return emptyCaseDetails(emptyMap());
    }

    public static CaseDetails emptyCaseDetails(Map<String, Object> placeholders) {
        String file = readFile("core-case-data-store-api/empty-case-details.json", placeholders);
        return convert(file, CaseDetails.class);
    }

    public static CaseDetails populatedCaseDetails() {
        return populatedCaseDetails(emptyMap());
    }

    public static CaseDetails populatedCaseDetails(Map<String, Object> placeholders) {
        String file = readFile("core-case-data-store-api/populated-case-details.json", placeholders);
        return convert(file, CaseDetails.class);
    }

    public static CallbackRequest callbackRequest() {
        return callbackRequest(emptyMap());
    }

    public static CallbackRequest callbackRequest(Map<String, Object> placeholders) {
        String file = readFile("core-case-data-store-api/callback-request.json", placeholders);
        return convert(file, CallbackRequest.class);
    }

    private static String readFile(String file, Map<String, Object> placeholders) {
        return StringSubstitutor.replace(ResourceReader.readString(file), placeholders);
    }

    private static <T> T convert(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
