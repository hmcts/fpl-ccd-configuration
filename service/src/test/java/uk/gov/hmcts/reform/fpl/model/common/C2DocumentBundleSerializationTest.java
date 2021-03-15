package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.SupplementType;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class C2DocumentBundleSerializationTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRoundTrip() {
        C2DocumentBundle initial = C2DocumentBundle.builder()
            .author("Author")
            .type(C2ApplicationType.WITH_NOTICE)
            .nameOfRepresentative("nameOfRepresentative")
            .usePbaPayment("usePbaPayment")
            .pbaNumber("pbaNumber")
            .clientCode("clientCode")
            .fileReference("fileReference")
            .document(DocumentReference.builder()
                .binaryUrl("binaryUrl")
                .filename("filename")
                .url("url")
                .build())
            .description("description")
            .uploadedDateTime("uploadedDatetime")
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
                    .build())))
            .supplementsBundle(List.of(element(UUID.fromString("dc6b2154-9e5d-480d-adca-d70b4e1f6385"),
                Supplement.builder()
                    .name(SupplementType.C13A_SPECIAL_GUARDIANSHIP)
                    .dateTimeUploaded(LocalDateTime.of(2013, 9, 10, 3, 4))
                    .document(DocumentReference.builder()
                        .binaryUrl("binaryUrl")
                        .filename("filename")
                        .url("url")
                        .build())
                    .uploadedBy("uploadedBy")
                    .build())))
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
                    "document_filename", "filename", "document_url", "url"
                )
            )
        ));

        List<Map<String, Object>> expectedSupplementBundle = List.of(Map.of(
            "id", "dc6b2154-9e5d-480d-adca-d70b4e1f6385",
            "value", Map.of(
                "name", SupplementType.C13A_SPECIAL_GUARDIANSHIP.toString(),
                "uploadedBy", "uploadedBy",
                "dateTimeUploaded", "2013-09-10T03:04:00",
                "document", Map.of(
                    "document_binary_url", "binaryUrl",
                    "document_filename", "filename", "document_url", "url"
                )
            )
        ));


        Map<String, Object> expectedBundle = Map.ofEntries(
            entry("usePbaPayment", "usePbaPayment"),
            entry("pbaNumber", "pbaNumber"),
            entry("type", "WITH_NOTICE"),
            entry("nameOfRepresentative", "nameOfRepresentative"),
            entry("clientCode", "clientCode"),
            entry("fileReference", "fileReference"),
            entry("document", Map.of(
                "document_binary_url", "binaryUrl",
                "document_filename", "filename",
                "document_url", "url"
                )
            ),
            entry("description", "description"),
            entry("uploadedDateTime", "uploadedDatetime"),
            entry("author", "Author"),
            entry("supportingEvidenceBundle", expectedBundles),
            entry("supportingEvidenceLA", expectedBundles),
            entry("supportingEvidenceNC", List.of()),
            entry("supplementsBundle", expectedSupplementBundle)
        );

        Map<String, Object> serialised = objectMapper.convertValue(initial, new TypeReference<>() {});
        C2DocumentBundle deserialised = objectMapper.convertValue(serialised, C2DocumentBundle.class);

        assertThat(serialised).isEqualTo(expectedBundle);
        assertThat(deserialised).isEqualTo(initial);
    }
}
