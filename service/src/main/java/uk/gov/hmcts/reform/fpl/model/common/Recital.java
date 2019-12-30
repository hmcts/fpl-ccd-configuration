package uk.gov.hmcts.reform.fpl.model.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Recital {
    private final String title;
    private final String description;
}
