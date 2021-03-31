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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class RespondentStatementTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRoundTrip() {
        String respondentName = "Sam Watson";
        UUID respondentId = UUID.randomUUID();

        RespondentStatement initial = RespondentStatement.builder()
            .respondentName(respondentName)
            .respondentId(respondentId)
            .supportingEvidenceBundle(List.of(element(
                UUID.fromString("dc6b2154-9e5d-480d-adca-d70b4e1f6384"),
                SupportingEvidenceBundle.builder()
                    .name("BundleName")
                    .dateTimeReceived(LocalDateTime.of(2012, 10, 10, 3, 4))
                    .dateTimeUploaded(LocalDateTime.of(2013, 9, 10, 3, 4))
                    .document(DocumentReference.builder()
                        .binaryUrl("binaryUrl")
                        .filename("filename")
                        .url("url")
                        .build())
                    .confidential(List.of("CONFIDENTIAL"))
                    .uploadedBy("uploadedBy")
                    .build()
            )))
            .build();

        List<Map<String, Object>> expectedBundles = List.of(Map.of(
            "id", "dc6b2154-9e5d-480d-adca-d70b4e1f6384",
            "value", Map.of(
                "confidential", List.of("CONFIDENTIAL"),
                "name", "BundleName",
                "uploadedBy", "uploadedBy",
                "confidentialTabLabel", "Confidential",
                "dateTimeReceived", "2012-10-10T03:04:00",
                "dateTimeUploaded", "2013-09-10T03:04:00",
                "document", Map.of(
                    "document_binary_url", "binaryUrl",
                    "document_filename", "filename",
                    "document_url", "url"
                )
            )
        ));

        Map<String, Object> expectedBundle = Map.of(
            "respondentName", respondentName,
            "respondentId", respondentId.toString(),
            "supportingEvidenceBundle", expectedBundles,
            "supportingEvidenceLA", expectedBundles,
            "supportingEvidenceNC", List.of()
        );

        Map<String, Object> serialised = objectMapper.convertValue(initial, new TypeReference<>() {});

        RespondentStatement deserialised = objectMapper.convertValue(serialised,
            RespondentStatement.class);

        assertThat(serialised).isEqualTo(expectedBundle);
        assertThat(deserialised).isEqualTo(initial);
    }
}
