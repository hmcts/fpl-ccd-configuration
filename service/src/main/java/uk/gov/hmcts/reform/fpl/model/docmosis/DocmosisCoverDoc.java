package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder(builderClassName = "Builder")
public class DocmosisCoverDoc {
    private final String familyManCaseNumber;
    private final String ccdCaseNumber;
    private final String representativeName;
    private final String representativeAddress;

    public Map<String, Object> toMap(ObjectMapper mapper) {
        return mapper.convertValue(this, new TypeReference<Map<String, Object>>() {});
    }
}
