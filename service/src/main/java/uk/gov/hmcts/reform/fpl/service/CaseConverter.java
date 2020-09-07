package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseConverter {

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
}
