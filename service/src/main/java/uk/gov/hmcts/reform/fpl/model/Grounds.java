package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
public class Grounds {
    @NotNull(message = "Select at least one option for how this case meets the threshold criteria")
    @Size(min = 1, message = "Select at least one option for how this case meets the threshold criteria")
    private final List<@NotBlank(message = "Select at least one option for how this case meets the threshold criteria")
        String> thresholdReason;
    @NotBlank(message = "Enter details of how the case meets the threshold criteria")
    private final String thresholdDetails;
}
