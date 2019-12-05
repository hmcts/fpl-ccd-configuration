package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.NextHearingType;
import uk.gov.hmcts.reform.fpl.enums.Type;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderAction {
    private final Type type;
    private final NextHearingType nextHearingType;
    private final String changeRequestedByJudge;
}
