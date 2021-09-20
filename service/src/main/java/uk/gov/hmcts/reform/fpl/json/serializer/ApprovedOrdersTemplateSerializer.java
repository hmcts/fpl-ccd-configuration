package uk.gov.hmcts.reform.fpl.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.ApprovedOrdersTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public class ApprovedOrdersTemplateSerializer extends JsonSerializer<ApprovedOrdersTemplate> {

    @Override
    public void serialize(ApprovedOrdersTemplate value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {

        if (value == null) {
            return;
        }

        gen.writeStartObject();
        serializers.defaultSerializeField("orderList", value.getOrderList(), gen);
        serializers.defaultSerializeField("documentLinks", value.getDocumentLinks(), gen);
        serializers.defaultSerializeField("subjectLineWithHearingDate", value.getSubjectLineWithHearingDate(), gen);
        serializers.defaultSerializeField("respondentLastName", value.getLastName(), gen);
        serializers.defaultSerializeField("caseUrl", value.getCaseUrl(), gen);
        serializers.defaultSerializeField("digitalPreference", value.getDigitalPreference(), gen);

        List<Map<String, Object>> attachedDocuments = defaultIfNull(value.getAttachedDocuments(), List.of());

        for (int i = 0; i <= 10; i++) {
            Object val = attachedDocuments.size() > i ? attachedDocuments.get(i) : "";
            serializers.defaultSerializeField("attachedDocument" + (i + 1), val, gen);
        }
        gen.writeEndObject();
    }
}
