package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ChildRecoveryOrderGround;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class GroundsForChildRecoveryOrder {
    @Size(min = 1, message = "Select at least one option for how this case meets grounds for a child recovery order")
    private final List<ChildRecoveryOrderGround> grounds;

    @NotBlank(message = "Please give reasons for the application of the order sought")
    private final String reason;
}
