package uk.gov.hmcts.reform.fpl.model.common;

import lombok.Builder;
import lombok.Data;

/**
 * A recital in the CMO.
 *
 * @deprecated to be removed with {@link uk.gov.hmcts.reform.fpl.model.CaseManagementOrder}
 */
@Data
@Builder(toBuilder = true)
@Deprecated(since = "FPLA-1915")
public class Recital {
    private final String title;
    private final String description;
}
