package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class LocalAuthorityName {
    String code;
    String name;
}
