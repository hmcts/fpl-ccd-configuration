package uk.gov.hmcts.reform.fpl.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.GroundsList;

import java.util.List;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Grounds {
    /**
    * @deprecated (DFPL-2312)
    */
    @Deprecated(since = "DFPL-2312")
    private final List<String> thresholdReason;
    @NotNull(message = "Select at least one option for how this case meets the threshold criteria")
    @Size(min = 1, message = "Select at least one option for how this case meets the threshold criteria")
    private final List<GroundsList> groundsReason;
    private final String hasThresholdDocument;
    @NotBlank(message = "Enter details of how the case meets the threshold criteria")
    private final String thresholdDetails;
}
