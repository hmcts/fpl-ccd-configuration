package uk.gov.hmcts.reform.fpl.json.serializer;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.ApprovedOrdersTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApproveOrdersTemplateSerializerTest extends SerializerTest {

    ApproveOrdersTemplateSerializerTest() {
        super(ApprovedOrdersTemplate.class, new ApprovedOrdersTemplateSerializer());
    }

    @Test
    void shouldDefaultSerializeFields() {
        ApprovedOrdersTemplate template = ApprovedOrdersTemplate.builder()
            .lastName("Smith").build();

        Map<String, Object> templateMap = mapper.convertValue(template, new TypeReference<>() {});
        assertThat(templateMap).containsEntry("respondentLastName", "Smith");
    }

    @Test
    void shouldCreateAttachedDocumentFieldsFromList() {
        Map<String, Object> map1 = Map.of("key1", "value1");
        Map<String, Object> map2 = Map.of("key2", "value2");

        List<Map<String, Object>> attachedDocuments = List.of(map1, map2);

        ApprovedOrdersTemplate template = ApprovedOrdersTemplate.builder()
            .attachedDocuments(attachedDocuments)
            .build();

        Map<String, Object> templateMap = mapper.convertValue(template, new TypeReference<>() {});

        assertThat(templateMap)
            .doesNotContainKey("attachedDocuments")
            .containsEntry("attachedDocument1", map1)
            .containsEntry("attachedDocument2", map2)
            .containsEntry("attachedDocument3", "");
    }
}
