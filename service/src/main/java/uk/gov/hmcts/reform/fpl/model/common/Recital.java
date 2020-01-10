package uk.gov.hmcts.reform.fpl.model.common;

import ccd.sdk.types.ComplexType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@ComplexType(name = "Recitals")
public class Recital {
    private final String title;
    private final String description;
}
