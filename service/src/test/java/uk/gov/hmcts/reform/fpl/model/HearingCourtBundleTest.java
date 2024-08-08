package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.YesNo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.DOCUMENT_ACKNOWLEDGEMENT_KEY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
public class HearingCourtBundleTest {
    private static final UUID TEST_ID = UUID.randomUUID();
    private static final String TEST_HEARING = "Test hearing";
    private static final String CONFIDENTIAL = "CONFIDENTIAL";

    private static final CourtBundle CONFIDENTIAL_COURT_BUNDLE = CourtBundle.builder()
        .document(testDocumentReference())
        .confidential(List.of(CONFIDENTIAL))
        .build();

    private static final CourtBundle NON_CONFIDENTIAL_COURT_BUNDLE = CourtBundle.builder()
        .document(testDocumentReference())
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
                    "document_binary_url", CONFIDENTIAL_COURT_BUNDLE.getDocument().getBinaryUrl(),
                    "document_filename", CONFIDENTIAL_COURT_BUNDLE.getDocument().getFilename(),
                    "document_url", CONFIDENTIAL_COURT_BUNDLE.getDocument().getUrl()
                )),
                Map.entry("documentAcknowledge", List.of(DOCUMENT_ACKNOWLEDGEMENT_KEY)),
                Map.entry("hasConfidentialAddress", YesNo.NO.getValue())
            )));

        Map<String, Object> expectedHearingCourtBundle = Map.of(
            "hearing", TEST_HEARING,
            "courtBundle", expectedCourtBundle
        );

        assertThat(serialised).isEqualTo(expectedHearingCourtBundle);
        assertThat(deserialised).isEqualTo(initialHearingCourtBundle);
    }

    @Test
    void testSerialisationAndDeserialisationIfEmptyBundle() {
        HearingCourtBundle initialHearingCourtBundle = HearingCourtBundle.builder()
            .hearing(TEST_HEARING)
            .courtBundle(List.of(element(
                TEST_ID,
                CourtBundle.builder()
                    .build()
            )))
            .build();

        Map<String, Object> serialised = objectMapper.convertValue(initialHearingCourtBundle, new TypeReference<>() {});
        HearingCourtBundle deserialised = objectMapper.convertValue(serialised, HearingCourtBundle.class);

        List<Map<String, Object>> expectedCourtBundle = List.of(Map.of(
            "id", TEST_ID.toString(),
            "value", Map.ofEntries(
                Map.entry("documentAcknowledge", List.of())
            )));

        Map<String, Object> expectedHearingCourtBundle = Map.of(
            "hearing", TEST_HEARING,
            "courtBundle", expectedCourtBundle
        );

        assertThat(serialised).isEqualTo(expectedHearingCourtBundle);
        assertThat(deserialised).isEqualTo(initialHearingCourtBundle);
    }
}
