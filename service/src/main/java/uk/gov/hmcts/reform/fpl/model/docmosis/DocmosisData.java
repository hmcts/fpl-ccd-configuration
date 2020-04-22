package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public interface DocmosisData {
    default Map<String, Object> toMap(ObjectMapper mapper) {
        return mapper.convertValue(this, new TypeReference<>() {
        });
    }
}
