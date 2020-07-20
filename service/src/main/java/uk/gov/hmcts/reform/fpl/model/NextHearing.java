package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Pointer to the next hearing after the CMO.
 *
 * @deprecated to be removed with {@link uk.gov.hmcts.reform.fpl.model.CaseManagementOrder}
 */
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Deprecated(since = "FPLA-1915")
@SuppressWarnings("java:S1133") // Remove once deprecations dealt with
public class NextHearing {
    private final UUID id;
    private final String date;
}
