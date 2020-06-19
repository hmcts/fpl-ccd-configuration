package uk.gov.hmcts.reform.fpl.model.common;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.types.ComplexType;

@Data
@Builder(toBuilder = true)
@ComplexType(name = "Recitals")
public class Recital {
    private final String title;
    private final String description;
}
