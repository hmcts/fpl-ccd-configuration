package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime;

@Getter
@Jacksonized
@Builder
public class ChildExtension {
    private CaseExtensionTime caseExtensionTimeList;
    private CaseExtensionReasonList caseExtensionReasonList;
}
