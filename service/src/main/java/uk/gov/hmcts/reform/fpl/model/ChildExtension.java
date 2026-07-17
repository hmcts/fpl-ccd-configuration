package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime;
import uk.gov.hmcts.reform.fpl.validation.groups.CaseExtensionGroup;

import java.time.LocalDate;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Value
@Jacksonized
@Builder
@JsonInclude(value = NON_NULL)
public class ChildExtension {
    @CCD(label = "Time extension.")
    CaseExtensionTime caseExtensionTimeList;
    @CCD(label = "Select reason for extension for:")
    CaseExtensionReasonList caseExtensionReasonList;
    @CCD(label = "New end date", showCondition = "caseExtensionTimeList=\"OtherExtension\"")
    @FutureOrPresent(message = "Enter an end date in the future", groups = CaseExtensionGroup.class)
    LocalDate extensionDateOther;
    @CCD(label = " ")
    String label;
    @CCD(label = " ", typeOverride = FieldType.Text)
    UUID id;
    @CCD(label = " ")
    String index;
}
