package uk.gov.hmcts.reform.fpl.service.orders;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class BaseDocmosisParameters implements DocmosisParameters {
    String title;
    String whatever;
}
