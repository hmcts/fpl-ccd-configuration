package uk.gov.hmcts.reform.fpl.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.ApprovedOrdersTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
        serializers.defaultSerializeField("respondentLastName", value.getRespondentLastName(), gen);
        serializers.defaultSerializeField("caseUrl", value.getCaseUrl(), gen);
        serializers.defaultSerializeField("digitalPreference", value.getDigitalPreference(), gen);

        List<Map<String, Object>> attachedDocuments = value.getAttachedDocuments();

        for (int i = 0; i < attachedDocuments.size(); i++) {
            serializers.defaultSerializeField("attachedDocument" + (i + 1), attachedDocuments.get(i), gen);
        }
        gen.writeEndObject();
    }
}
