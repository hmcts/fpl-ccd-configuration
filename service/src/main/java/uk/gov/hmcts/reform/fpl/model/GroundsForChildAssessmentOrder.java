package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
public class GroundsForChildAssessmentOrder {
    @NotBlank(message = "Enter details of how the case meets the threshold criteria")
    private final String thresholdDetails;
}
