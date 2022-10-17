package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime;
import uk.gov.hmcts.reform.fpl.validation.groups.CaseExtensionGroup;

import java.time.LocalDate;
import java.util.UUID;
import javax.validation.constraints.FutureOrPresent;

@Value
@Jacksonized
@Builder
public class ChildExtension {
    CaseExtensionTime caseExtensionTimeList;
    CaseExtensionReasonList caseExtensionReasonList;
    @FutureOrPresent(message = "Enter an end date in the future", groups = CaseExtensionGroup.class)
    LocalDate extensionDateOther;
    String label;
    UUID id;
}
