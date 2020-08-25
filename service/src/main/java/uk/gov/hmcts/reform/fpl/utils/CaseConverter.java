package uk.gov.hmcts.reform.fpl.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseConverter {
    private final ObjectMapper mapper;

    public Map<String, Object> convertToMap(CaseData caseData) {
        return mapper.convertValue(caseData, new TypeReference<>() {});
    }

    public CaseData convertToCaseData(CaseDetails caseDetails) {
        return mapper.convertValue(caseDetails.getData(), CaseData.class);
    }
}
