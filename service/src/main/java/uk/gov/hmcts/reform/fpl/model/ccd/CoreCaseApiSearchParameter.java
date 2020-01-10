package uk.gov.hmcts.reform.fpl.model.ccd;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class CoreCaseApiSearchParameter {
    private final String event;
    private final String jurisdiction;
    private final String caseType;
    private final Long caseId;
}
