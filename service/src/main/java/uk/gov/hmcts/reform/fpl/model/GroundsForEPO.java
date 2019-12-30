package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.validation.groups.EPOGroup;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
