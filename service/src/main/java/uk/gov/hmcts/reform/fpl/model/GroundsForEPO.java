package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.validation.groups.EPOGroup;

import java.util.List;

@Data
@Builder
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class GroundsForEPO {
    @NotNull(message = "Select at least one option for how this case meets grounds for an emergency protection order",
        groups = EPOGroup.class)
    @Size(min = 1,
        message = "Select at least one option for how this case meets grounds for an emergency protection order",
        groups = EPOGroup.class)
    private List<@NotBlank(
        message = "Select at least one option for how this case meets grounds for an emergency protection order",
        groups = EPOGroup.class) String> reason;
}
