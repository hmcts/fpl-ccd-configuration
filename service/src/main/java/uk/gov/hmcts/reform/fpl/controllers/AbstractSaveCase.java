package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor(force = true)
public abstract class AbstractSaveCase {

    private final ObjectMapper mapper;

    public AboutToStartOrSubmitCallbackResponse saveCase(CaseData caseData) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(convertToMap(caseData))
            .build();
    }

    public Map<String, Object> convertToMap(CaseData caseData) {
        return mapper.convertValue(caseData, new TypeReference<>() {});
    }

    public CaseData convertToCaseData(CaseDetails caseDetails) {
        return mapper.convertValue(caseDetails.getData(), CaseData.class);
    }
}
