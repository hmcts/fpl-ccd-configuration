package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class GroundsForEPO {
    private List<String> reason;
    private List<String> thresholdReason;
    private String thresholdDetails;
}
