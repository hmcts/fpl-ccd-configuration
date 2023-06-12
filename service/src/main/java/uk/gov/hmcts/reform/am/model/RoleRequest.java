package uk.gov.hmcts.reform.am.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleRequest {

    private String assignerId;

    @Builder.Default
    private String process = "fpl-case-service";
    private String reference;

    @Builder.Default
    private boolean replaceExisting = false;

}
