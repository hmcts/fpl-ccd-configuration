package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class CallbackResponse {
    CaseData caseData;
    Map<String, Object> data;
    List<String> errors;
}
