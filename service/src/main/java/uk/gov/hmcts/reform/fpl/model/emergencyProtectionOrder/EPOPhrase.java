package uk.gov.hmcts.reform.fpl.model.emergencyProtectionOrder;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class EPOPhrase {
    private final String includePhrase;
}
