package uk.gov.hmcts.reform.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
public class FeeDto {
    private double calculatedAmount;
    private String version;
    private String code;
}
