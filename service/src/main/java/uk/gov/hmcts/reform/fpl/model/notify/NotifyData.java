package uk.gov.hmcts.reform.fpl.model.notify;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface NotifyData {
    //TODO it should net be here, conversions should be responsibility of NotificationService
    default Map<String, Object> toMap(ObjectMapper mapper) {
        return mapper.convertValue(this, new TypeReference<>() {
        });
    }
}
