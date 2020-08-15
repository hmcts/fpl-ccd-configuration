package uk.gov.hmcts.reform.fpl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseDataConverter {
    private final ObjectMapper mapper;

    @SuppressWarnings("unchecked")
    public Map<String, Object> convertToMap(CaseData caseData) {
        return (Map<String, Object>) mapper.convertValue(caseData, Map.class);
    }
}
