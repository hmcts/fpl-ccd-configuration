package uk.gov.hmcts.reform.ccd.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CaseLocation {

    private String baseLocation;

    private String region;
}
