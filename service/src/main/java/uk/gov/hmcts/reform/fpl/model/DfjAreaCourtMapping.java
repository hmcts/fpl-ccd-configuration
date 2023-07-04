package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class DfjAreaCourtMapping {
    private final String courtCode;
    private final String courtField;
    private final String dfjArea;
}
