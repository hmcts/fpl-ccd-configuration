package uk.gov.hmcts.reform.ccd.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CaseLocation {

    private String baseLocation;

    private String region;
}
