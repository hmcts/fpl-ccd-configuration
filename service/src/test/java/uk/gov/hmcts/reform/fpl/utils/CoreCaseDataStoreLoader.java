package uk.gov.hmcts.reform.fpl.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.apache.commons.text.StringSubstitutor;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import static java.util.Collections.emptyMap;

public class CoreCaseDataStoreLoader {

    private static final ObjectMapper mapper = JsonMapper.builder()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .addModule(new ParameterNamesModule())
        .addModule(new Jdk8Module())
        .addModule(new JavaTimeModule())
        .build();

    private static final CaseConverter caseConverter = new CaseConverter(mapper);

    private CoreCaseDataStoreLoader() {
        new ObjectMapper();        // NO-OP
    }

    public static CaseConverter getCaseConverterInstance() {
        return caseConverter;
    }

    public static CaseData emptyCaseData() {
        return caseConverter.convert(emptyCaseDetails());
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

    public static CaseData populatedCaseData() {
        CaseDetails caseDetails = populatedCaseDetails();
        return caseConverter.convert(caseDetails);
    }

    public static CaseData populatedCaseData(Map<String, Object> placeholders) {
        CaseDetails caseDetails = populatedCaseDetails(placeholders);
        return caseConverter.convert(caseDetails);
    }

    public static CaseData caseData() {
        return caseConverter.convert(callbackRequest(emptyMap()).getCaseDetails());
    }

    public static CaseData caseData(Map<String, Object> placeholders) {
        return caseConverter.convert(callbackRequest(placeholders).getCaseDetails());
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
