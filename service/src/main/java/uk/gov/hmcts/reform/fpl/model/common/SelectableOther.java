package uk.gov.hmcts.reform.fpl.model.common;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class SelectableOther {
    String code;
    String name;
}
