package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseConverter {

    public static TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public CaseData convert(CaseDetails caseDetails) {
        if (isNull(caseDetails)) {
            return null;
        }
        return objectMapper.convertValue(caseDetails.getData(), CaseData.class)
            .toBuilder()
            .state(State.tryFromValue(caseDetails.getState()).orElse(null))
            .id(caseDetails.getId())
            .build();
    }

    public CaseDetails convert(CaseData caseData) {
        if (isNull(caseData)) {
            return null;
        }
        return CaseDetails.builder()
            .id(caseData.getId())
            .state(Optional.ofNullable(caseData.getState()).map(State::getValue).orElse(null))
            .data(objectMapper.convertValue(caseData, MAP_TYPE))
            .build();
    }

    public <T> Map<String, Object> toMap(T object) {
        if (isNull(object)) {
            return null;
        }
        return objectMapper.convertValue(object, MAP_TYPE);
    }

}
