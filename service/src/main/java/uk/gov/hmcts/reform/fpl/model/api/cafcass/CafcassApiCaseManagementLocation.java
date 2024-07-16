package uk.gov.hmcts.reform.fpl.model.api.cafcass;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CafcassApiCaseManagementLocation {
    private String region;
    private String baseLocation;
}
