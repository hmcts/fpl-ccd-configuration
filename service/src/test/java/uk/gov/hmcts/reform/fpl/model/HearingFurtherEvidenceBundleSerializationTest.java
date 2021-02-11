package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CaseConverter.class, JacksonAutoConfiguration.class})
class HearingFurtherEvidenceBundleSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaseConverter caseConverter;

    @Test
    void testRoundTrip() {
        HearingFurtherEvidenceBundle initial = HearingFurtherEvidenceBundle.builder()
            .hearingName("HearingName")
            .supportingEvidenceBundle(List.of(Element.<SupportingEvidenceBundle>builder()
                .id(UUID.fromString("dc6b2154-9e5d-480d-adca-d70b4e1f6384"))
                .value(SupportingEvidenceBundle.builder()
                    .name("BundleName")
                    .dateTimeReceived(LocalDateTime.of(2012, 10, 10, 3, 4))
                    .dateTimeUploaded(LocalDateTime.of(2013, 9, 10, 3, 4))
                    .document(DocumentReference.builder()
                        .binaryUrl("binaryUrl")
                        .filename("filename")
                        .url("url")
                        .build())
                    .confidential(List.of("confidential"))
                    .uploadedBy("uploadedBy")
                    .build())
                .build()))
            .build();

        Map<String, Object> serialised = caseConverter.toMap(initial);

        HearingFurtherEvidenceBundle deserialised = objectMapper.convertValue(serialised,
            HearingFurtherEvidenceBundle.class);

        List<Map<String, Object>> expectedBundles = List.of(Map.of("id", "dc6b2154-9e5d-480d-adca-d70b4e1f6384",
            "value", Map.of("confidential", List.of("confidential"),
                "name", "BundleName",
                "uploadedBy", "uploadedBy",
                "confidentialTabLabel", "Confidential",
                "dateTimeReceived", "2012-10-10T03:04:00",
                "dateTimeUploaded", "2013-09-10T03:04:00",
                "document", Map.of("document_binary_url", "binaryUrl",
                    "document_filename", "filename", "document_url", "url")
            )));

        Map<String, Object> expectedBundle = Map.of(
            "hearingName", "HearingName",
            "supportingEvidenceBundle", expectedBundles,
            "supportingEvidenceLA", expectedBundles,
            "supportingEvidenceNC", expectedBundles
        );

        assertThat(serialised).isEqualTo(expectedBundle);
        assertThat(deserialised).isEqualTo(initial);
    }
}
