package uk.gov.hmcts.reform.fpl.model.emergencyProtectionOrder;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EPOChildren {
    private String descriptionNeeded;
    private String description;
}
