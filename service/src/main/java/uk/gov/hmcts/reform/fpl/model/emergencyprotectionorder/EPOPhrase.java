package uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EPOPhrase {
    private String includePhrase;
}
