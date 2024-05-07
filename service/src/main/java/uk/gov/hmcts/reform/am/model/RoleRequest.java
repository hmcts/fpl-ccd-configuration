package uk.gov.hmcts.reform.am.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@NoArgsConstructor
@Jacksonized
@AllArgsConstructor
public class RoleRequest {

    private String assignerId;

    @Builder.Default
    private String process = "fpl-case-service";
    private String reference;

    @Builder.Default
    private boolean replaceExisting = false;

}
