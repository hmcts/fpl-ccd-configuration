package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CallbackResponse {

    private final CaseData caseData;
    private final List<String> errors;
}
