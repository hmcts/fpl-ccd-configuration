package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ChildRecoveryOrderGround;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;


@Data
@Builder
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class GroundsForChildRecoveryOrder {
    @CCD(
            label = "The grounds are that the child[ren]",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "ChildRecoveryOrderGroundList"
    )
    @Size(min = 1, message = "Select at least one option for how this case meets grounds for a child recovery order")
    private final List<ChildRecoveryOrderGround> grounds;

    @CCD(label = "The reason(s) for the application", typeOverride = FieldType.TextArea)
    @NotBlank(message = "Please give reasons for the application of the order sought")
    private final String reason;
}
