package uk.gov.hmcts.reform.fpl.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Grounds {
    @NotNull(message = "Select at least one option for how this case meets the threshold criteria")
    @Size(min = 1, message = "Select at least one option for how this case meets the threshold criteria")
    private final List<@NotBlank(message = "Select at least one option for how this case meets the threshold criteria")
        String> thresholdReason;
    private final String hasThresholdDocument;
    private final String thresholdDetails;
}
