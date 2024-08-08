package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CafcassApiCaseManagementLocation {
    private String region;
    private String baseLocation;
}
