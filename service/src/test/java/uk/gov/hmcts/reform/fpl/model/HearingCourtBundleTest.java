package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
public class HearingCourtBundleTest {
    private static final UUID TEST_ID = UUID.randomUUID();
    private static final String TEST_HEARING = "Test hearing";
    private static final String CONFIDENTIAL = "CONFIDENTIAL";

    private static final CourtBundle CONFIDENTIAL_COURT_BUNDLE = CourtBundle.builder()
        .document(DocumentReference.builder()
            .binaryUrl("binaryUrl")
            .filename("filename")
            .url("url")
            .build())
        .confidential(List.of(CONFIDENTIAL))
        .build();

    private static final CourtBundle NON_CONFIDENTIAL_COURT_BUNDLE = CourtBundle.builder()
        .document(DocumentReference.builder()
            .binaryUrl("binaryUrl")
            .filename("filename")
            .url("url")
            .build())
        .confidential(emptyList())
        .build();

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerialisationAndDeserialisation() {
        HearingCourtBundle initialHearingCourtBundle = HearingCourtBundle.builder()
            .hearing(TEST_HEARING)
            .courtBundle(List.of(element(
                TEST_ID,
                CONFIDENTIAL_COURT_BUNDLE
            )))
            .build();

        Map<String, Object> serialised = objectMapper.convertValue(initialHearingCourtBundle, new TypeReference<>() {});
        HearingCourtBundle deserialised = objectMapper.convertValue(serialised,
            HearingCourtBundle.class);

        List<Map<String, Object>> expectedCourtBundle = List.of(Map.of(
            "id", TEST_ID.toString(),
            "value", Map.ofEntries(
                Map.entry("confidential", List.of(CONFIDENTIAL)),
                Map.entry("document", Map.of(
                    "document_binary_url", "binaryUrl",
                    "document_filename", "filename",
                    "document_url", "url"
                ))
            )));

        Map<String, Object> expectedHearingCourtBundle = Map.of(
            "hearing", TEST_HEARING,
            "courtBundle", expectedCourtBundle
        );

        assertThat(serialised).isEqualTo(expectedHearingCourtBundle);
        assertThat(deserialised).isEqualTo(initialHearingCourtBundle);
    }

    @Test
    void testCourtBundleNCContainsNonConfidentialBundleOnly() {
        HearingCourtBundle hearingCourtBundle = HearingCourtBundle.builder()
            .hearing(TEST_HEARING)
            .courtBundle(List.of(
                element(TEST_ID, NON_CONFIDENTIAL_COURT_BUNDLE),
                element(TEST_ID, CONFIDENTIAL_COURT_BUNDLE)
            ))
            .build();

        assertThat(unwrapElements(hearingCourtBundle.getCourtBundle()))
            .isEqualTo(List.of(NON_CONFIDENTIAL_COURT_BUNDLE, CONFIDENTIAL_COURT_BUNDLE));
    }
}
